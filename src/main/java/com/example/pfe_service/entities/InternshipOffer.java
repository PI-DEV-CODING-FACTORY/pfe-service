package com.example.pfe_service.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String companyId; // Keycloak user ID for Company
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "internship_offer_technologies",
        joinColumns = @JoinColumn(name = "internship_offer_id")
    )
    @Column(name = "technology")
    @Enumerated(EnumType.STRING)
    private List<Technologies> requiredTechnologies = new ArrayList<>();
    
    @OneToMany(mappedBy = "internshipOffer", cascade = CascadeType.ALL)
    private List<StudentInterest> interestedStudents = new ArrayList<>();
} 