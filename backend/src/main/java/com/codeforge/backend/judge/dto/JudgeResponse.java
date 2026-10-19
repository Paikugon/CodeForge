package com.codeforge.backend.judge.dto;

import tools.jackson.databind.JsonNode;

public record JudgeResponse(
        String err,
        JsonNode data) {
}