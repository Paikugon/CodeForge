package com.codeforge.backend.judge.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.codeforge.backend.judge.dto.JudgeRequest;
import com.codeforge.backend.judge.dto.JudgeResponse;

@Component
public class JudgeClient {

    private final RestClient restClient;

    public JudgeClient(
            @Value("${judge.server.url}") String serverUrl,
            @Value("${judge.server.token}") String token) {

        this.restClient = RestClient.builder()
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