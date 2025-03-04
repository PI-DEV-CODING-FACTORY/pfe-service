package com.example.pfe_service.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroqResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    @Data
    public static class Choice {
        private Integer index;
        private Map<String, String> message;
        private String finishReason;
    }

    @Data
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
} 