package com.example.pfe_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewRequest {
    private String studentEmail;
    private LocalDateTime interviewDateTime;
    private String message;
} 