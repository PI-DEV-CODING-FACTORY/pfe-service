package com.example.pfe_service.repository;

import com.example.pfe_service.entities.TechnicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicalTestRepository extends JpaRepository<TechnicalTest, Long> {

}
