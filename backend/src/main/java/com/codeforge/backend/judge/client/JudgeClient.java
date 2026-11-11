package com.codeforge.backend.judge.client;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.codeforge.backend.judge.dto.JudgeRequest;
import com.codeforge.backend.judge.dto.JudgeResponse;
import com.codeforge.backend.judge.dto.JudgeRunRequest;

@Component
public class JudgeClient {

    private final RestClient restClient;

    public JudgeClient(
            @Value("${judge.server.url}") String serverUrl,
            @Value("${judge.server.token}") String token,
            @Value("${judge.server.connect-timeout-ms:5000}") long connectTimeoutMs,
            @Value("${judge.server.read-timeout-ms:300000}") long readTimeoutMs) {

        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "Judge Server timeouts must be positive");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(
                Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(serverUrl)
                .defaultHeader(
                        "X-Judge-Server-Token",
                        sha256(token))
                .build();
    }

    public JudgeResponse judge(JudgeRequest request) {
        JudgeResponse response = restClient.post()
                .uri("/judge")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JudgeResponse.class);

        if (response == null) {
            throw new IllegalStateException("Judge Server returned an empty response");
        }

        return response;
    }

    public JudgeResponse run(JudgeRunRequest request) {
        JudgeResponse response = restClient.post()
                .uri("/judge")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JudgeResponse.class);

        if (response == null) {
            throw new IllegalStateException("Judge Server returned an empty response");
        }

        return response;
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}