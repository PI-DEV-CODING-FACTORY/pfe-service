package com.example.pfe_service.dto;

import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Technologies;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PfeFilterRequest {
    private String titleContains;
    private String descriptionContains;
    private List<Technologies> technologies;
    private OpenFor openFor;
    private String studentId;
    private Boolean processing;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;
} 