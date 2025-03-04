package com.example.pfe_service.service;

import com.example.pfe_service.entities.StudentInterest;
import java.util.List;

public interface IStudentInterestService {
    StudentInterest createStudentInterest(StudentInterest studentInterest);
    StudentInterest updateStudentInterest(StudentInterest studentInterest);
    void deleteStudentInterest(Long id);
    StudentInterest getStudentInterestById(Long id);
    List<StudentInterest> getAllStudentInterests();
    List<StudentInterest> getStudentInterestsByStudentId(String studentId);
    List<StudentInterest> getStudentInterestsByInternshipOfferId(Long internshipOfferId);
} 