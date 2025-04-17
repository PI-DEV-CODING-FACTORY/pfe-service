package com.example.pfe_service.controller;

import com.example.pfe_service.dto.ProposalCreateRequest;
import com.example.pfe_service.dto.InterviewRequest;
import com.example.pfe_service.entities.Proposal;
import com.example.pfe_service.entities.ProposalStatus;
import com.example.pfe_service.entities.TechnicalTest;
import com.example.pfe_service.service.IProposalService;
import com.example.pfe_service.service.ITechnicalTestService;
import com.example.pfe_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProposalController {
    private final IProposalService proposalService;
    private final ITechnicalTestService technicalTestService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Proposal> createProposal(@RequestBody ProposalCreateRequest request) {
        return new ResponseEntity<>(proposalService.createProposal(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateProposalStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        ProposalStatus newStatus = ProposalStatus.valueOf(statusUpdate.get("status").toUpperCase());
        Long technicalTestId = proposalService.updateProposalStatus(id, newStatus);
        
        if (newStatus == ProposalStatus.ACCEPTED && technicalTestId != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Proposal accepted and technical test created",
                "technicalTestId", technicalTestId
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Proposal status updated to " + newStatus
        ));
    }
    
    @PostMapping("/{id}/accept-proposal")
    public ResponseEntity<?> acceptProposal(@PathVariable Long id) {
        // Accept the proposal and create a technical test with questions based on PFE technologies
        Long technicalTestId = proposalService.acceptProposalAndCreateTest(id);
        
        if (technicalTestId != null) {
            TechnicalTest technicalTest = technicalTestService.getTechnicalTestById(technicalTestId);
            return ResponseEntity.ok(Map.of(
                "message", "Proposal accepted and technical test created with questions based on PFE technologies",
                "technicalTestId", technicalTestId,
                "technicalTest", technicalTest
            ));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "message", "Failed to create technical test"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProposal(@PathVariable Long id) {
        proposalService.deleteProposal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> getProposalById(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.getProposalById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Proposal>> getProposalsByStudentId(@PathVariable String studentId) {
        List<Proposal> proposals = proposalService.getProposalsByStudentId(studentId);
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Proposal>> getProposalsByCompanyId(@PathVariable String companyId) {
        List<Proposal> proposals = proposalService.getProposalsByCompanyId(companyId);
        return ResponseEntity.ok(proposals);
    }

    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposals() {
        return ResponseEntity.ok(proposalService.getAllProposals());
    }

    @PostMapping("/{id}/send-interview-invitation")
    public ResponseEntity<Map<String, String>> sendInterviewInvitation(
            @PathVariable Long id,
            @RequestBody InterviewRequest request,
            @RequestHeader("X-Company-Id") String companyId) {
        try {
            // Get the proposal to verify it exists and belongs to the company
            Proposal proposal = proposalService.getProposalById(id);
            
            // Verify the proposal belongs to the company
            if (!proposal.getCompanyId().equals(companyId)) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to send interview invitation for this proposal"));
            }
            
            // Send the interview invitation
            emailService.sendInterviewInvitation(
                request.getStudentEmail(),
                request.getInterviewDateTime(),
                request.getMessage()
            );
            
            // Update proposal status to MEETING_SCHEDULED
            proposalService.updateProposalStatus(id, ProposalStatus.MEETING_SCHEDULED);
            
            return ResponseEntity.ok(Map.of(
                "message", "Interview invitation sent successfully and proposal status updated"
            ));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to send interview invitation: " + e.getMessage()));
        }
    }
}
