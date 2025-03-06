package com.example.pfe_service.repository;

import com.example.pfe_service.entities.TechnicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalTestRepository extends JpaRepository<TechnicalTest, Long> {
    @Query("SELECT t FROM TechnicalTest t JOIN t.proposal p WHERE p.studentId = :studentId")
    List<TechnicalTest> findByStudentId(@Param("studentId") String studentId);
}
