package com.codeforge.backend.tag;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.problem.repository.ProblemRepository;
import com.codeforge.backend.tag.model.Tag;
import com.codeforge.backend.tag.repository.TagRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:codeforge-tag-api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
class TagApiTests {
    @LocalServerPort
    private int port;
    @Autowired private TagRepository repository;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtEncoder encoder;
    @Autowired private ObjectMapper mapper;
    private final HttpClient client = HttpClient.newHttpClient();
    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() {
        for (String table : List.of("problem_tags", "submissions", "test_cases", "problems", "tags", "users")) {
            jdbc.update("delete from " + table);
        }
        adminToken = token("tagadmin", "ADMIN");
        memberToken = token("tagmember", "USER");
    }

    @Test
    void publicListAndDetailUseDtoFieldsAndStableNameOrdering() throws Exception {
        assertEquals("[]", send("GET", "/api/tags", null, null).body());
        Tag graphs = repository.saveAndFlush(new Tag("Graphs"));
        repository.saveAndFlush(new Tag("Arrays"));
        var response = send("GET", "/api/tags", null, null);
        assertEquals(200, response.statusCode());
        JsonNode list = mapper.readTree(response.body());
        assertEquals(2, list.size());
        assertEquals("Arrays", list.get(0).get("name").asText());
        var detail = send("GET", "/api/tags/" + graphs.getId(), null, null);
        assertEquals(200, detail.statusCode());
        assertEquals(2, mapper.readTree(detail.body()).size());
        assertEquals("Graphs", mapper.readTree(detail.body()).get("name").asText());
    }

    @Test
    void expiredTokenUsesPublicAccessOnlyOnPublicEndpoints() throws Exception {
        Tag tag = repository.saveAndFlush(new Tag("Graphs"));
        String expiredToken = token("expired", "USER", Instant.now().minusSeconds(600));

        var publicResponse = send("GET", "/api/tags/" + tag.getId(), null, expiredToken);
        assertEquals(200, publicResponse.statusCode());
        assertEquals("expired", publicResponse.headers().firstValue("X-Auth-Status").orElseThrow());

        var protectedResponse = send("GET", "/api/users/profile/me", null, expiredToken);
        assertEquals(401, protectedResponse.statusCode());
        assertTrue(protectedResponse.headers().firstValue("X-Auth-Status").isEmpty());
    }

    @Test
    void adminCanCreateUpdateAndDeleteTags() throws Exception {
        var created = send("POST", "/api/tags", "{\"name\":\"  Arrays  \"}", adminToken);
        assertEquals(201, created.statusCode());
        JsonNode body = mapper.readTree(created.body());
        long id = body.get("id").asLong();
        assertEquals("Arrays", body.get("name").asText());
        assertEquals("/api/tags/" + id, created.headers().firstValue("Location").orElseThrow());
        var updated = send("PUT", "/api/tags/" + id, "{\"name\":\"Array problems\"}", adminToken);
        assertEquals(200, updated.statusCode());
        assertEquals("Array problems", mapper.readTree(updated.body()).get("name").asText());
        assertEquals(204, send("DELETE", "/api/tags/" + id, null, adminToken).statusCode());
        assertEquals(404, send("GET", "/api/tags/" + id, null, null).statusCode());
    }

    @Test
    void mutationsRequireAnAuthenticatedAdmin() throws Exception {
        Tag tag = repository.saveAndFlush(new Tag("Arrays"));
        for (String method : List.of("POST", "PUT", "DELETE")) {
            String path = "/api/tags" + (method.equals("POST") ? "" : "/" + tag.getId());
            String body = method.equals("DELETE") ? null : "{\"name\":\"Graphs\"}";
            assertEquals(401, send(method, path, body, null).statusCode());
            assertEquals(403, send(method, path, body, memberToken).statusCode());
        }
        assertEquals(1, repository.count());
        assertEquals("Arrays", repository.findById(tag.getId()).orElseThrow().getName());
    }

    @Test
    void invalidNamesAndBodiesAreRejected() throws Exception {
        for (String body : List.of(
                "{\"name\":\" \"}",
                "{\"name\":\"" + "x".repeat(51) + "\"}",
                "{}", "{not-json}")) {
            assertEquals(400, send("POST", "/api/tags", body, adminToken).statusCode(), body);
        }
        Tag tag = repository.saveAndFlush(new Tag("Arrays"));
        assertEquals(400, send("PUT", "/api/tags/" + tag.getId(), "{}", adminToken).statusCode());
        assertEquals("Arrays", repository.findById(tag.getId()).orElseThrow().getName());
        assertEquals(1, repository.count());
    }

    @Test
    void renamePreservesOtherTags() throws Exception {
        repository.saveAndFlush(new Tag("Arrays"));
        Tag graphs = repository.saveAndFlush(new Tag("Graphs"));
        String body = "{\"name\":\"Array problems\"}";
        var response = send("PUT", "/api/tags/" + graphs.getId(), body, adminToken);
        assertEquals(200, response.statusCode());
        assertEquals("Array problems", mapper.readTree(response.body()).get("name").asText());
        assertEquals(2, repository.count());
        assertEquals("Array problems", repository.findById(graphs.getId()).orElseThrow().getName());
    }

    @Test
    void unknownTagsReturnNotFound() throws Exception {
        for (String method : List.of("GET", "PUT", "DELETE")) {
            var response = send(method, "/api/tags/999999", method.equals("PUT")
                    ? "{\"name\":\"Arrays\"}" : null, adminToken);
            assertEquals(404, response.statusCode());
            assertEquals("CODE_TAG_NOT_FOUND", mapper.readTree(response.body()).get("message").asText());
        }
    }

    @Test
    void deletingASharedTagOnlyRemovesItsAssignments() throws Exception {
        Tag shared = repository.saveAndFlush(new Tag("Arrays"));
        Tag other = repository.saveAndFlush(new Tag("Graphs"));
        Problem first = problemRepository.saveAndFlush(new Problem("First", "first", "First", 1000, 256));
        Problem second = problemRepository.saveAndFlush(new Problem("Second", "second", "Second", 1000, 256));
        jdbc.update("insert into problem_tags(problem_id,tag_id) values (?,?),(?,?),(?,?)",
                first.getId(), shared.getId(), second.getId(), shared.getId(), second.getId(), other.getId());
        assertEquals(204, send("DELETE", "/api/tags/" + shared.getId(), null, adminToken).statusCode());
        assertEquals(2, problemRepository.count());
        assertEquals(1, repository.count());
        assertEquals(1, jdbc.queryForObject("select count(*) from problem_tags", Integer.class));
        assertEquals(other.getId(), jdbc.queryForObject("select tag_id from problem_tags", Long.class));
    }

    private String token(String username, String role) {
        return token(username, role, Instant.now().plusSeconds(600));
    }

    private String token(String username, String role, Instant expiresAt) {
        jdbc.update("insert into users(username,email,password_hash,role,enabled,must_change_password,token_version,created_at,updated_at) values (?,?,?,?,true,false,0,current_timestamp,current_timestamp)",
                username, username + "@example.com", "test-only", role);
        Long id = jdbc.queryForObject("select id from users where username=?", Long.class, username);
        Instant issuedAt = expiresAt.minusSeconds(600);
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().subject(id.toString()).issuedAt(issuedAt).expiresAt(expiresAt)
                        .claim("token_version", 0).build())).getTokenValue();
    }

    private HttpResponse<String> send(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
