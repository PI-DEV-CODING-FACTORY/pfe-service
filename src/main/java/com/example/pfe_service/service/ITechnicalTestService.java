package com.example.pfe_service.service;

import com.example.pfe_service.dto.TestSubmissionRequest;
import com.example.pfe_service.entities.Question;
import com.example.pfe_service.entities.TechnicalTest;
import java.util.List;

public interface ITechnicalTestService {
    TechnicalTest createTechnicalTest(TechnicalTest technicalTest);
    TechnicalTest updateTechnicalTest(TechnicalTest technicalTest);
    void deleteTechnicalTest(Long id);
    TechnicalTest getTechnicalTestById(Long id);
    List<TechnicalTest> getAllTechnicalTests();
    List<TechnicalTest> getTechnicalTestsByStudentId(String studentId);
    List<Question> getQuestionsByTechnicalTestId(Long technicalTestId);
    TechnicalTest submitTest(TestSubmissionRequest request);
}
