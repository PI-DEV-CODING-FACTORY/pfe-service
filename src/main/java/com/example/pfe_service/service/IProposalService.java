package com.example.pfe_service.service;

import com.example.pfe_service.dto.ProposalCreateRequest;
import com.example.pfe_service.entities.Proposal;
import com.example.pfe_service.entities.ProposalStatus;
import java.util.List;

public interface IProposalService {
    Proposal createProposal(ProposalCreateRequest request);
    Long updateProposalStatus(Long id, ProposalStatus status);
    Long acceptProposalAndCreateTest(Long id);
    void deleteProposal(Long id);
    Proposal getProposalById(Long id);
    List<Proposal> getAllProposals();
    List<Proposal> getProposalsByStudentId(String studentId);
}
