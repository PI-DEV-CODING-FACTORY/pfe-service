package com.example.pfe_service.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    private Boolean isMultipleChoice;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] options;
    
    @Column(columnDefinition = "TEXT")
    private String correctAnswer;
    
    @Column(columnDefinition = "TEXT")
    private String userAnswer;
    
    private Boolean isCorrect;
    private Integer points;
    private String technology;

    @ManyToOne
    @JoinColumn(name = "technical_test_id")
    private TechnicalTest technicalTest;
}