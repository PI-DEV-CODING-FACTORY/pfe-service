package com.example.pfe_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GroqRequest {
    private String model;
    private List<Map<String, String>> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
} 