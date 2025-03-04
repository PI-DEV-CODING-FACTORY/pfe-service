package com.example.pfe_service.service;

import com.example.pfe_service.entities.StudentInterest;
import com.example.pfe_service.repository.StudentInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentInterestService implements IStudentInterestService {
    private final StudentInterestRepository studentInterestRepository;

    @Override
    public StudentInterest createStudentInterest(StudentInterest studentInterest) {
        log.info("Creating new student interest");
        return studentInterestRepository.save(studentInterest);
    }

    @Override
    public StudentInterest updateStudentInterest(StudentInterest studentInterest) {
        log.info("Updating student interest with id: {}", studentInterest.getId());
        if (!studentInterestRepository.existsById(studentInterest.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student interest not found");
        }
        return studentInterestRepository.save(studentInterest);
    }

    @Override
    public void deleteStudentInterest(Long id) {
        log.info("Deleting student interest with id: {}", id);
        if (!studentInterestRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student interest not found");
        }
        studentInterestRepository.deleteById(id);
    }

    @Override
    public StudentInterest getStudentInterestById(Long id) {
        log.info("Fetching student interest with id: {}", id);
        return studentInterestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student interest not found"));
    }

    @Override
    public List<StudentInterest> getAllStudentInterests() {
        log.info("Fetching all student interests");
        return studentInterestRepository.findAll();
    }

    @Override
    public List<StudentInterest> getStudentInterestsByStudentId(String studentId) {
        log.info("Fetching student interests for student id: {}", studentId);
        return studentInterestRepository.findByStudentId(studentId);
    }

    @Override
    public List<StudentInterest> getStudentInterestsByInternshipOfferId(Long internshipOfferId) {
        log.info("Fetching student interests for internship offer id: {}", internshipOfferId);
        return studentInterestRepository.findByInternshipOfferId(internshipOfferId);
    }
} 