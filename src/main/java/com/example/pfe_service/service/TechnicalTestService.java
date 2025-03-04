package com.example.pfe_service.service;

import com.example.pfe_service.repository.ProposalRepository;
import com.example.pfe_service.repository.TechnicalTestRepository;
import com.example.pfe_service.entities.TechnicalTest;
import com.example.pfe_service.entities.Question;
import com.example.pfe_service.entities.Proposal;
import com.example.pfe_service.entities.Technologies;
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
        return technicalTestRepository.save(technicalTest);
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
        return technicalTestRepository.save(technicalTest);
    }

    @Override
    public void deleteTechnicalTest(Long id) {
        log.info("Deleting technical test with id: {}", id);
        technicalTestRepository.deleteById(id);
    }

    @Override
    public TechnicalTest getTechnicalTestById(Long id) {
        log.info("Fetching technical test with id: {}", id);
        return technicalTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technical test not found with id: " + id));
    }

    @Override
    public List<TechnicalTest> getAllTechnicalTests() {
        log.info("Fetching all technical tests");
        return technicalTestRepository.findAll();
    }
}
