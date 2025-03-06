package com.example.pfe_service.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private LocalDateTime finishedAt;
    private Duration timeSpent; // Time taken to complete the test
    private Boolean cheated;
    private Boolean isCompleted;
    private Integer score;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "technical_test_technologies",
        joinColumns = @JoinColumn(name = "technical_test_id")
    )
    @Column(name = "technology")
    @Enumerated(EnumType.STRING)
    private Set<Technologies> technologies = new LinkedHashSet<>();

    @OneToOne
    @JoinColumn(name = "proposal_id")
    private Proposal proposal;

    @OneToMany(mappedBy = "technicalTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        cheated = false;
    }
}
