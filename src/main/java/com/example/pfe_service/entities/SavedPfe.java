package com.example.pfe_service.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedPfe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pfe_id")
    private Pfe pfe;

    private String companyId; // Keycloak user ID for Company
}