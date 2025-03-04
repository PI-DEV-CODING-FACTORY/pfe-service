package com.example.pfe_service.controller;

import com.example.pfe_service.entities.StudentInterest;
import com.example.pfe_service.service.IStudentInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-interests")
@RequiredArgsConstructor
public class StudentInterestController {
    private final IStudentInterestService studentInterestService;

    @PostMapping
    public ResponseEntity<StudentInterest> createStudentInterest(@RequestBody StudentInterest studentInterest) {
        StudentInterest createdInterest = studentInterestService.createStudentInterest(studentInterest);
        return new ResponseEntity<>(createdInterest, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentInterest> updateStudentInterest(
            @PathVariable Long id,
            @RequestBody StudentInterest studentInterest) {
        studentInterest.setId(id);
        StudentInterest updatedInterest = studentInterestService.updateStudentInterest(studentInterest);
        return ResponseEntity.ok(updatedInterest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentInterest(@PathVariable Long id) {
        studentInterestService.deleteStudentInterest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentInterest> getStudentInterestById(@PathVariable Long id) {
        StudentInterest studentInterest = studentInterestService.getStudentInterestById(id);
        return ResponseEntity.ok(studentInterest);
    }

    @GetMapping
    public ResponseEntity<List<StudentInterest>> getAllStudentInterests() {
        List<StudentInterest> interests = studentInterestService.getAllStudentInterests();
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentInterest>> getStudentInterestsByStudentId(@PathVariable String studentId) {
        List<StudentInterest> interests = studentInterestService.getStudentInterestsByStudentId(studentId);
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/internship-offer/{internshipOfferId}")
    public ResponseEntity<List<StudentInterest>> getStudentInterestsByInternshipOfferId(
            @PathVariable Long internshipOfferId) {
        List<StudentInterest> interests = studentInterestService.getStudentInterestsByInternshipOfferId(internshipOfferId);
        return ResponseEntity.ok(interests);
    }
} 