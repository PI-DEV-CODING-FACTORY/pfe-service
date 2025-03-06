package com.example.pfe_service.service;

import com.example.pfe_service.repository.ProposalRepository;
import com.example.pfe_service.repository.TechnicalTestRepository;
import com.example.pfe_service.entities.TechnicalTest;
import com.example.pfe_service.entities.Question;
import com.example.pfe_service.entities.Proposal;
import com.example.pfe_service.entities.Technologies;
import com.example.pfe_service.dto.TestSubmissionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TechnicalTestService implements ITechnicalTestService {
    private final TechnicalTestRepository technicalTestRepository;
    private final ProposalRepository proposalRepository;
    private final GroqService groqService;

    @Override
    @Transactional
    public TechnicalTest createTechnicalTest(TechnicalTest technicalTest) {
        log.info("Creating new technical test");
        return cleanTechnicalTest(technicalTestRepository.save(technicalTest));
    }

    @Transactional
    public Long createTechnicalTest(Proposal proposal) {
        log.info("Creating new technical test for proposal: {}", proposal.getId());
        
        // Create a new technical test
        TechnicalTest technicalTest = new TechnicalTest();
        technicalTest.setProposal(proposal);
        technicalTest.setCreatedAt(LocalDateTime.now());
        technicalTest.setDeadline(LocalDateTime.now().plusDays(7)); // Give 7 days to complete
        technicalTest.setIsCompleted(false);
        technicalTest.setScore(0);
        
        // Set title and description
        String pfeTitle = proposal.getPfe().getTitle();
        technicalTest.setTitle("Technical Test for " + pfeTitle);
        technicalTest.setDescription("This technical test is based on the PFE: " + pfeTitle);
        
        // Set technologies from the PFE - convert List to Set
        Set<Technologies> techSet = new LinkedHashSet<>(proposal.getPfe().getTechnologies());
        technicalTest.setTechnologies(techSet);
        
        // Generate questions using Groq based on PFE details
        List<Question> questions = groqService.generateTechnicalQuestions(
            proposal.getPfe().getTechnologies(),
            proposal.getPfe().getDescription()
        );
        
        technicalTest.setQuestions(questions);
        
        // Save the test
        TechnicalTest savedTest = technicalTestRepository.save(technicalTest);
        
        // Set the technical test reference for each question
        questions.forEach(q -> q.setTechnicalTest(savedTest));
        
        return savedTest.getId();
    }

    @Override
    public TechnicalTest updateTechnicalTest(TechnicalTest technicalTest) {
        log.info("Updating technical test with id: {}", technicalTest.getId());
        return cleanTechnicalTest(technicalTestRepository.save(technicalTest));
    }

    @Override
    public void deleteTechnicalTest(Long id) {
        log.info("Deleting technical test with id: {}", id);
        technicalTestRepository.deleteById(id);
    }

    @Override
    public TechnicalTest getTechnicalTestById(Long id) {
        log.info("Fetching technical test with id: {}", id);
        TechnicalTest originalTest = technicalTestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Technical test not found with id: " + id));
        
        // Create a minimal version with essential fields
        TechnicalTest cleanedTest = new TechnicalTest();
        cleanedTest.setId(originalTest.getId());
        cleanedTest.setTitle(originalTest.getTitle());
        cleanedTest.setDescription(originalTest.getDescription());
        cleanedTest.setIsCompleted(originalTest.getIsCompleted());
        cleanedTest.setScore(originalTest.getScore());
        cleanedTest.setFinishedAt(originalTest.getFinishedAt());
        cleanedTest.setTimeSpent(originalTest.getTimeSpent());
        cleanedTest.setCheated(originalTest.getCheated());
        
        // Clean questions to prevent circular reference
        if (originalTest.getQuestions() != null) {
            List<Question> cleanedQuestions = originalTest.getQuestions().stream()
                .map(q -> {
                    Question cleaned = new Question();
                    cleaned.setId(q.getId());
                    cleaned.setText(q.getText());
                    cleaned.setExplanation(q.getExplanation());
                    cleaned.setIsMultipleChoice(q.getIsMultipleChoice());
                    cleaned.setOptions(q.getOptions());
                    cleaned.setCorrectAnswer(q.getCorrectAnswer());
                    cleaned.setUserAnswer(q.getUserAnswer());
                    cleaned.setIsCorrect(q.getIsCorrect());
                    cleaned.setPoints(q.getPoints());
                    // Don't set technicalTest to prevent circular reference
                    return cleaned;
                })
                .collect(Collectors.toList());
            cleanedTest.setQuestions(cleanedQuestions);
        }
        
        // Only include company ID from proposal
        if (originalTest.getProposal() != null) {
            Proposal cleanedProposal = new Proposal();
            cleanedProposal.setCompanyId(originalTest.getProposal().getCompanyId());
            cleanedTest.setProposal(cleanedProposal);
        }
        
        return cleanedTest;
    }

    @Override
    public List<TechnicalTest> getAllTechnicalTests() {
        log.info("Fetching all technical tests");
        return technicalTestRepository.findAll().stream()
                .map(this::cleanTechnicalTest)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TechnicalTest> getTechnicalTestsByStudentId(String studentId) {
        log.info("Fetching technical tests for student ID: {}", studentId);
        return technicalTestRepository.findByStudentId(studentId).stream()
                .map(this::cleanTechnicalTestWithQuestionIds)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Question> getQuestionsByTechnicalTestId(Long technicalTestId) {
        log.info("Fetching questions for technical test ID: {}", technicalTestId);
        TechnicalTest technicalTest = technicalTestRepository.findById(technicalTestId)
                .orElseThrow(() -> new RuntimeException("Technical test not found with id: " + technicalTestId));
        return technicalTest.getQuestions();
    }
    
    // Helper method to clean technical test data before returning
    private TechnicalTest cleanTechnicalTest(TechnicalTest originalTest) {
        if (originalTest == null) {
            return null;
        }
        
        TechnicalTest cleanedTest = new TechnicalTest();
        cleanedTest.setId(originalTest.getId());
        cleanedTest.setTitle(originalTest.getTitle());
        cleanedTest.setDescription(originalTest.getDescription());
        cleanedTest.setCreatedAt(originalTest.getCreatedAt());
        cleanedTest.setDeadline(originalTest.getDeadline());
        cleanedTest.setFinishedAt(originalTest.getFinishedAt());
        cleanedTest.setTimeSpent(originalTest.getTimeSpent());
        cleanedTest.setCheated(originalTest.getCheated());
        cleanedTest.setIsCompleted(originalTest.getIsCompleted());
        cleanedTest.setScore(originalTest.getScore());
        cleanedTest.setTechnologies(originalTest.getTechnologies());
        // Questions are excluded from the response
        
        // Only include essential proposal information
        if (originalTest.getProposal() != null) {
            Proposal cleanedProposal = new Proposal();
            cleanedProposal.setId(originalTest.getProposal().getId());
            cleanedProposal.setCompanyId(originalTest.getProposal().getCompanyId());
            cleanedTest.setProposal(cleanedProposal);
        }
        
        return cleanedTest;
    }
    
    // Helper method to clean technical test data and include only question IDs
    private TechnicalTest cleanTechnicalTestWithQuestionIds(TechnicalTest originalTest) {
        if (originalTest == null) {
            return null;
        }
        
        TechnicalTest cleanedTest = new TechnicalTest();
        cleanedTest.setId(originalTest.getId());
        cleanedTest.setTitle(originalTest.getTitle());
        cleanedTest.setDescription(originalTest.getDescription());
        cleanedTest.setCreatedAt(originalTest.getCreatedAt());
        cleanedTest.setDeadline(originalTest.getDeadline());
        cleanedTest.setFinishedAt(originalTest.getFinishedAt());
        cleanedTest.setTimeSpent(originalTest.getTimeSpent());
        cleanedTest.setCheated(originalTest.getCheated());
        cleanedTest.setIsCompleted(originalTest.getIsCompleted());
        cleanedTest.setScore(originalTest.getScore());
        cleanedTest.setTechnologies(originalTest.getTechnologies());
        
        // Create simplified questions with only IDs
        if (originalTest.getQuestions() != null) {
            List<Question> simplifiedQuestions = originalTest.getQuestions().stream()
                .map(q -> {
                    Question simplified = new Question();
                    simplified.setId(q.getId());
                    return simplified;
                })
                .collect(Collectors.toList());
            cleanedTest.setQuestions(simplifiedQuestions);
        }
        
        // Only include essential proposal information
        if (originalTest.getProposal() != null) {
            Proposal cleanedProposal = new Proposal();
            cleanedProposal.setId(originalTest.getProposal().getId());
            cleanedProposal.setCompanyId(originalTest.getProposal().getCompanyId());
            cleanedTest.setProposal(cleanedProposal);
        }
        
        return cleanedTest;
    }

    @Override
    @Transactional
    public TechnicalTest submitTest(TestSubmissionRequest request) {
        log.info("Processing test submission for technical test ID: {}", request.getTechnicalTestId());
        
        if (request.getTechnicalTestId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technical test ID is required");
        }
        
        TechnicalTest technicalTest = technicalTestRepository.findById(request.getTechnicalTestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Technical test not found with id: " + request.getTechnicalTestId()));
        
        if (technicalTest.getIsCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This test has already been completed");
        }
        
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No answers provided");
        }
        
        try {
            // Create a map of question IDs to answers for easy lookup
            Map<Long, String> answerMap = request.getAnswers().stream()
                    .collect(Collectors.toMap(
                        TestSubmissionRequest.QuestionAnswer::getQuestionId,
                        answer -> answer.getAnswer() != null ? answer.getAnswer() : ""
                    ));
            
            int totalScore = 0;
            int totalPoints = 0;
            
            // Process each question
            for (Question question : technicalTest.getQuestions()) {
                String userAnswer = answerMap.getOrDefault(question.getId(), "");
                
                question.setUserAnswer(userAnswer);
                totalPoints += question.getPoints();
                
                try {
                    if (question.getIsMultipleChoice()) {
                        // For multiple choice questions, direct comparison
                        boolean isCorrect = userAnswer.equals(question.getCorrectAnswer());
                        question.setIsCorrect(isCorrect);
                        if (isCorrect) {
                            totalScore += question.getPoints();
                        }
                    } else {
                        // For open-ended questions, use Groq to verify
                        boolean isCorrect = groqService.verifyAnswer(
                            question.getCorrectAnswer(),
                            userAnswer,
                            question.getText()
                        );
                        question.setIsCorrect(isCorrect);
                        if (isCorrect) {
                            totalScore += question.getPoints();
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing answer for question {}: {}", question.getId(), e.getMessage());
                    // If there's an error verifying the answer, be lenient and mark it as correct
                    question.setIsCorrect(true);
                    totalScore += question.getPoints();
                }
            }
            
            // Update test status
            technicalTest.setIsCompleted(true);
            technicalTest.setFinishedAt(LocalDateTime.now());
            technicalTest.setTimeSpent(request.getTimeSpent());
            technicalTest.setCheated(request.getCheated() != null ? request.getCheated() : false);
            
            // Calculate final score as a percentage
            int finalScore = totalPoints > 0 ? (totalScore * 100) / totalPoints : 0;
            technicalTest.setScore(finalScore);
            
            TechnicalTest savedTest = technicalTestRepository.save(technicalTest);
            return getTechnicalTestById(savedTest.getId());
            
        } catch (Exception e) {
            log.error("Error processing test submission: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error processing test submission: " + e.getMessage());
        }
    }
}
