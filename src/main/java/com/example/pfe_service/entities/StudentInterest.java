package com.example.pfe_service.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentId; // Keycloak user ID for Student
    
    @ManyToOne
    @JoinColumn(name = "internship_offer_id")
    private InternshipOffer internshipOffer;
    
    private Boolean hasProposal;
    private Boolean proposalAccepted;
} 