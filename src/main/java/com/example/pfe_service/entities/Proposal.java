package com.example.pfe_service.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentId; // Keycloak user ID for Student
    private String companyId; // Keycloak user ID for Company
    
    @Enumerated(EnumType.STRING)
    private ProposalStatus status = ProposalStatus.PENDING;
    
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    
    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne
    @JoinColumn(name = "pfe_id")
    @JsonBackReference
    private Pfe pfe;

    @OneToOne(mappedBy = "proposal", cascade = CascadeType.ALL)
    private TechnicalTest technicalTest;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}