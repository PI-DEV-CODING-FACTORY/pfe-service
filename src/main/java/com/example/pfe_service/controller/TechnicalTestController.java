package com.example.pfe_service.controller;

import com.example.pfe_service.dto.TestSubmissionRequest;
import com.example.pfe_service.entities.Question;
import com.example.pfe_service.entities.TechnicalTest;
import com.example.pfe_service.service.ITechnicalTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technical-tests")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TechnicalTestController {
    private final ITechnicalTestService technicalTestService;

    @PostMapping
    public ResponseEntity<TechnicalTest> createTechnicalTest(@RequestBody TechnicalTest technicalTest) {
        TechnicalTest createdTest = technicalTestService.createTechnicalTest(technicalTest);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<TechnicalTest> updateTechnicalTest(@RequestBody TechnicalTest technicalTest) {
        TechnicalTest updatedTest = technicalTestService.updateTechnicalTest(technicalTest);
        return ResponseEntity.ok(updatedTest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnicalTest(@PathVariable Long id) {
        technicalTestService.deleteTechnicalTest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicalTest> getTechnicalTestById(@PathVariable Long id) {
        TechnicalTest technicalTest = technicalTestService.getTechnicalTestById(id);
        return ResponseEntity.ok(technicalTest);
    }

    @PostMapping("/submit")
    public ResponseEntity<TechnicalTest> submitTest(@RequestBody TestSubmissionRequest request) {
        TechnicalTest result = technicalTestService.submitTest(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TechnicalTest>> getTechnicalTestsByStudentId(@PathVariable String studentId) {
        List<TechnicalTest> technicalTests = technicalTestService.getTechnicalTestsByStudentId(studentId);
        return ResponseEntity.ok(technicalTests);
    }

    @GetMapping
    public ResponseEntity<List<TechnicalTest>> getAllTechnicalTests() {
        List<TechnicalTest> technicalTests = technicalTestService.getAllTechnicalTests();
        return ResponseEntity.ok(technicalTests);
    }
}
