package com.example.pfe_service.dto;

import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Technologies;
import lombok.Data;

import java.util.List;

@Data
public class PfeCreateRequest {
    private String title;
    private String description;
    private String githubUrl;
    private String videoUrl;
    private List<Technologies> technologies;
    private OpenFor openFor;
    private String studentId;
} 