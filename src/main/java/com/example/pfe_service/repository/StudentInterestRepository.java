package com.example.pfe_service.repository;

import com.example.pfe_service.entities.StudentInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentInterestRepository extends JpaRepository<StudentInterest, Long> {
    List<StudentInterest> findByStudentId(String studentId);
    List<StudentInterest> findByInternshipOfferId(Long internshipOfferId);
} 