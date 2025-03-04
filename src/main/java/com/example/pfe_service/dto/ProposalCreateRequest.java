package com.example.pfe_service.dto;

import lombok.Data;

@Data
public class ProposalCreateRequest {
    private String studentId;
    private String companyId;
    private Long pfeId;
    private String message;
} 