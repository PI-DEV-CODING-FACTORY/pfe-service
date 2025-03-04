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
    
    @Column(length = 1000)
    private String text;
    
    @Column(length = 1000)
    private String explanation;
    
    private Boolean isMultipleChoice;
    
    @Column(columnDefinition = "text[]")
    private String[] options;
    
    private String correctAnswer;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer points;

    @ManyToOne
    @JoinColumn(name = "technical_test_id")
    private TechnicalTest technicalTest;
}