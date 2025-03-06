package com.example.pfe_service.service;

import com.example.pfe_service.dto.ProposalCreateRequest;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Proposal;
import com.example.pfe_service.entities.ProposalStatus;
import com.example.pfe_service.exception.ResourceNotFoundException;
import com.example.pfe_service.repository.PfeRepository;
import com.example.pfe_service.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProposalService implements IProposalService {
    private final ProposalRepository proposalRepository;
    private final PfeRepository pfeRepository;
    private final TechnicalTestService technicalTestService;

    @Override
    public Proposal createProposal(ProposalCreateRequest request) {
        log.info("Creating proposal with studentId: {}, pfeId: {}", request.getStudentId(), request.getPfeId());
            
        Pfe pfe = pfeRepository.findById(request.getPfeId())
            .orElseThrow(() -> new ResourceNotFoundException("PFE not found with id: " + request.getPfeId()));

        Proposal proposal = new Proposal();
        proposal.setStudentId(request.getStudentId());
        proposal.setCompanyId(request.getCompanyId());
        proposal.setPfe(pfe);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setCreatedAt(LocalDateTime.now());
        proposal.setMessage(request.getMessage());

        return cleanProposal(proposalRepository.save(proposal));
    }

    @Override
    public Long updateProposalStatus(Long id, ProposalStatus status) {
        Proposal proposal = getProposalById(id);
        proposal.setStatus(status);
        proposal.setRespondedAt(LocalDateTime.now());
        
        proposalRepository.save(proposal);
        
        // If the proposal is accepted, create a technical test
        if (status == ProposalStatus.ACCEPTED) {
            return technicalTestService.createTechnicalTest(proposal);
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public Long acceptProposalAndCreateTest(Long id) {
        log.info("Accepting proposal and creating technical test for proposal id: {}", id);
        
        Proposal proposal = getProposalById(id);
        
        // Check if the proposal is already accepted
        if (proposal.getStatus() == ProposalStatus.ACCEPTED) {
            log.info("Proposal is already accepted, returning existing technical test");
            if (proposal.getTechnicalTest() != null) {
                return proposal.getTechnicalTest().getId();
            }
        }
        
        // Update the proposal status to ACCEPTED
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setRespondedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        
        // Create a technical test with questions based on the PFE technologies
        Long technicalTestId = technicalTestService.createTechnicalTest(proposal);
        
        log.info("Created technical test with id: {} for proposal: {}", technicalTestId, id);
        return technicalTestId;
    }

    @Override
    public void deleteProposal(Long id) {
        if (!proposalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proposal not found with id: " + id);
        }
        proposalRepository.deleteById(id);
    }

    @Override
    public Proposal getProposalById(Long id) {
        return cleanProposal(proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id)));
    }

    @Override
    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll().stream()
                .map(this::cleanProposal)
                .collect(Collectors.toList());
    }

    @Override
    public List<Proposal> getProposalsByStudentId(String studentId) {
        log.info("Fetching proposals for student ID: {}", studentId);
        return proposalRepository.findByStudentId(studentId).stream()
                .map(this::cleanProposal)
                .collect(Collectors.toList());
    }

    // Helper method to clean proposal data before returning
    private Proposal cleanProposal(Proposal originalProposal) {
        Proposal proposal = new Proposal();
        proposal.setId(originalProposal.getId());
        proposal.setStudentId(originalProposal.getStudentId());
        proposal.setCompanyId(originalProposal.getCompanyId());
        proposal.setPfe(originalProposal.getPfe());
        proposal.setStatus(originalProposal.getStatus());
        proposal.setCreatedAt(originalProposal.getCreatedAt());
        proposal.setRespondedAt(originalProposal.getRespondedAt());
        proposal.setMessage(originalProposal.getMessage());
        return proposal;
    }
}
