package com.example.pfe_service.repository;

import com.example.pfe_service.entities.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByStudentId(String studentId);
    List<Proposal> findByCompanyId(String companyId);
}
