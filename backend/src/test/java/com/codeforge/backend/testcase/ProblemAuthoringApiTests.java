package com.codeforge.backend.testcase;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.testcase.repository.TestCaseRepository;
import com.codeforge.backend.testcase.service.TestCaseSyncService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:codeforge-authoring-api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
class ProblemAuthoringApiTests {
    @LocalServerPort private int port;
    @Autowired private ProblemRepository problems;
    @Autowired private TestCaseRepository testCases;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtEncoder encoder;
    @Autowired private ObjectMapper mapper;
    @MockitoBean private TestCaseSyncService syncService;
    private final HttpClient client = HttpClient.newHttpClient();
    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() {
        for (String table : List.of("problem_tags", "submissions", "test_cases", "problems", "tags", "users")) {
            jdbc.update("delete from " + table);
        }
        adminToken = token("authoradmin", "ADMIN");
        memberToken = token("authormember", "USER");
        reset(syncService);
    }

    @Test
    void createsProblemAndNestedCasesAndOnlyExposesSamplesPublicly() throws Exception {
        var payload = problemPayload();
        payload.put("title", "  A plus B  ");
        payload.put("testCases", List.of(sample(), hidden()));
        var response = send("POST", "/api/problems", payload, adminToken);
        assertEquals(201, response.statusCode(), response.body());
        JsonNode body = mapper.readTree(response.body());
        long id = body.get("id").asLong();
        assertEquals("A plus B", body.get("title").asText());
        assertEquals(2, body.get("testCases").size());
        assertEquals(0, body.at("/testCases/0/orderIndex").asInt());
        assertEquals(1, body.at("/testCases/1/orderIndex").asInt());
        assertEquals(2, testCases.countByProblemId(id));
        verify(syncService, times(1)).syncProblem(id);

        var publicResponse = send("GET", "/api/problems/" + id, null, null);
        assertEquals(200, publicResponse.statusCode());
        assertEquals(1, mapper.readTree(publicResponse.body()).get("testCases").size());
        assertFalse(publicResponse.body().contains("999 1"));
    }

    @Test
    void invalidNestedCaseLeavesNoProblemAndDoesNotSync() throws Exception {
        var payload = problemPayload();
        payload.put("testCases", List.of(sample(), Map.of("input", "invalid")));
        var response = send("POST", "/api/problems", payload, adminToken);
        assertEquals(400, response.statusCode());
        assertEquals("CODE_EXPECTED_OUTPUT_REQUIRED",
                mapper.readTree(response.body()).get("fieldErrors").get("testCases[1].expectedOutput").asText());
        assertEquals(0, problems.count());
        assertEquals(0, testCases.count());
        verifyNoInteractions(syncService);
    }

    @Test
    void serviceFailureRollsBackNewProblemAndEveryNestedCase() throws Exception {
        var payload = problemPayload();
        payload.put("testCases", List.of(
                Map.of("input", "1", "expectedOutput", "1", "orderIndex", Integer.MAX_VALUE), hidden()));
        var response = send("POST", "/api/problems", payload, adminToken);
        assertEquals(400, response.statusCode(), response.body());
        assertEquals("CODE_TEST_CASE_ORDER_EXHAUSTED", mapper.readTree(response.body()).get("message").asText());
        assertEquals(0, problems.count());
        assertEquals(0, testCases.count());
        verifyNoInteractions(syncService);
    }

    @Test
    void batchAppendsIndexesAndSendsOneSyncEvent() throws Exception {
        long id = problemId();
        assertEquals(201, send("POST", casesPath(id), sample(), adminToken).statusCode());
        clearInvocations(syncService);
        var response = send("POST", casesPath(id) + "/batch",
                Map.of("testCases", List.of(hidden(), sample())), adminToken);
        assertEquals(201, response.statusCode(), response.body());
        JsonNode body = mapper.readTree(response.body());
        assertEquals(1, body.get(0).get("orderIndex").asInt());
        assertEquals(2, body.get(1).get("orderIndex").asInt());
        assertEquals(3, testCases.countByProblemId(id));
        verify(syncService, times(1)).syncProblem(id);
    }

    @Test
    void explicitIndexesAreReservedAndEmptyInputOutputRemainValid() throws Exception {
        long id = problemId();
        var response = send("POST", casesPath(id) + "/batch", Map.of("testCases", List.of(
                Map.of("input", "", "expectedOutput", ""),
                Map.of("input", "1", "expectedOutput", "1", "orderIndex", 7))), adminToken);
        assertEquals(201, response.statusCode(), response.body());
        JsonNode body = mapper.readTree(response.body());
        assertEquals(8, body.get(0).get("orderIndex").asInt());
        assertEquals(7, body.get(1).get("orderIndex").asInt());
    }

    @Test
    void invalidBatchDoesNotPersistItsValidItems() throws Exception {
        long id = problemId();
        for (Object invalid : List.of(Map.of("input", "missing-output"),
                Map.of("input", "a", "expectedOutput", "b", "orderIndex", -1))) {
            var response = send("POST", casesPath(id) + "/batch",
                    Map.of("testCases", List.of(sample(), invalid)), adminToken);
            assertEquals(400, response.statusCode(), response.body());
        }
        var withNull = new ArrayList<Object>();
        withNull.add(sample());
        withNull.add(null);
        assertEquals(400, send("POST", casesPath(id) + "/batch",
                Map.of("testCases", withNull), adminToken).statusCode());
        assertEquals(0, testCases.countByProblemId(id));
        verifyNoInteractions(syncService);
    }

    @Test
    void rejectsMissingEmptyAndOversizedBatches() throws Exception {
        long id = problemId();
        for (Object payload : List.of(Map.of(), Map.of("testCases", List.of()),
                Map.of("testCases", java.util.Collections.nCopies(101, sample())))) {
            assertEquals(400, send("POST", casesPath(id) + "/batch", payload, adminToken).statusCode());
        }
        assertEquals(0, testCases.count());
        verifyNoInteractions(syncService);
    }

    @Test
    void mutationsRequireAnAdminAndUnknownProblemReturns404() throws Exception {
        long id = problemId();
        var payload = Map.of("testCases", List.of(sample()));
        assertEquals(401, send("POST", casesPath(id) + "/batch", payload, null).statusCode());
        assertEquals(403, send("POST", casesPath(id) + "/batch", payload, memberToken).statusCode());
        assertEquals(404, send("POST", casesPath(Long.MAX_VALUE) + "/batch", payload, adminToken).statusCode());
        assertEquals(0, testCases.count());
    }

    @Test
    void validatesProblemBoundsAndTagIdsBeforePersistence() throws Exception {
        for (var change : List.of(Map.entry("title", (Object) "   "), Map.entry("description", (Object) "\n\t"),
                Map.entry("timeLimitMs", (Object) 0), Map.entry("timeLimitMs", (Object) 30001),
                Map.entry("memoryLimitMb", (Object) 1025), Map.entry("difficulty", (Object) "IMPOSSIBLE"),
                Map.entry("slug", (Object) "Invalid Slug"), Map.entry("tagIds", (Object) List.of(-1)))) {
            var payload = problemPayload();
            payload.put(change.getKey(), change.getValue());
            assertEquals(400, send("POST", "/api/problems", payload, adminToken).statusCode(), change.toString());
        }
        var payload = problemPayload();
        payload.put("tagIds", List.of(Long.MAX_VALUE));
        assertEquals(404, send("POST", "/api/problems", payload, adminToken).statusCode());
        assertEquals(0, problems.count());
    }

    @Test
    void editingCasePreservesOmittedSampleFlagAndOrder() throws Exception {
        long id = problemId();
        var created = send("POST", casesPath(id), Map.of("input", "1", "expectedOutput", "1",
                "isSample", true, "orderIndex", 9), adminToken);
        long caseId = mapper.readTree(created.body()).get("id").asLong();
        var edited = send("PUT", casesPath(id) + "/" + caseId,
                Map.of("input", "2", "expectedOutput", "2"), adminToken);
        assertEquals(200, edited.statusCode());
        JsonNode body = mapper.readTree(edited.body());
        assertTrue(body.get("isSample").asBoolean());
        assertEquals(9, body.get("orderIndex").asInt());
    }

    @Test
    void problemEditRejectsNestedCasesWithoutChangingProblem() throws Exception {
        long id = problemId();
        var payload = problemPayload();
        payload.put("testCases", List.of(sample()));
        var response = send("PUT", "/api/problems/" + id, payload, adminToken);
        assertEquals(400, response.statusCode());
        assertEquals("CODE_TEST_CASES_CREATE_ONLY", mapper.readTree(response.body()).get("message").asText());
        assertEquals("Existing", problems.findById(id).orElseThrow().getTitle());
        assertEquals(0, testCases.count());
    }

    @Test
    void concurrentBatchesGetDifferentAutomaticIndexes() throws Exception {
        long id = problemId();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> send("POST", casesPath(id) + "/batch",
                    Map.of("testCases", List.of(sample(), hidden())), adminToken));
            var second = executor.submit(() -> send("POST", casesPath(id) + "/batch",
                    Map.of("testCases", List.of(sample(), hidden())), adminToken));
            assertEquals(201, first.get().statusCode());
            assertEquals(201, second.get().statusCode());
        }
        assertEquals(List.of(0, 1, 2, 3), testCases.findByProblemIdOrderByOrderIndexAscIdAsc(id)
                .stream().map(t -> t.getOrderIndex()).toList());
    }

    private long problemId() {
        return problems.saveAndFlush(new Problem("Existing", "existing", "Description", 1000, 256)).getId();
    }

    private Map<String, Object> problemPayload() {
        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("title", "A plus B"); payload.put("slug", "a-plus-b");
        payload.put("description", "Print a + b.");
        return payload;
    }

    private Map<String, Object> sample() {
        return Map.of("input", "1 2\n", "expectedOutput", "3\n", "isSample", true);
    }
    private Map<String, Object> hidden() {
        return Map.of("input", "999 1\n", "expectedOutput", "1000\n");
    }
    private String casesPath(long id) { return "/api/problems/" + id + "/test-cases"; }

    private String token(String username, String role) {
        jdbc.update("insert into users(username,email,password_hash,role,enabled,must_change_password,token_version,created_at,updated_at) values (?,?,?,?,true,false,0,current_timestamp,current_timestamp)",
                username, username + "@example.com", "test-only", role);
        Long id = jdbc.queryForObject("select id from users where username=?", Long.class, username);
        Instant now = Instant.now();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().subject(id.toString()).issuedAt(now).expiresAt(now.plusSeconds(600))
                        .claim("token_version", 0).build())).getTokenValue();
    }

    private HttpResponse<String> send(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
