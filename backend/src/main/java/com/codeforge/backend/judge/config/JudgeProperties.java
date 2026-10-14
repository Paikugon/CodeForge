package com.codeforge.backend.judge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "judge.server")
public record JudgeProperties(
    String url,
    String token
) {}
