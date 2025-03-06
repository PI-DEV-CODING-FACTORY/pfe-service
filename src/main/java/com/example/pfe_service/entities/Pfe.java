package com.example.pfe_service.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pfe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String rapportUrl;
    
    @Column(columnDefinition = "TEXT")
    private String githubUrl;
    
    @Column(columnDefinition = "TEXT")
    private String videoUrl;
    
    private Boolean processing;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "pfe_technologies",
        joinColumns = @JoinColumn(name = "pfe_id")
    )
    @Column(name = "technology")
    @Enumerated(EnumType.STRING)
    private List<Technologies> technologies = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private OpenFor openFor;
    
    private String studentId; // Keycloak user ID for Student
    
    @OneToMany(mappedBy = "pfe", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Proposal> proposals = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
