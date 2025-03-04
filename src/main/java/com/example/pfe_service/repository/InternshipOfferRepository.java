package com.example.pfe_service.repository;

import com.example.pfe_service.entities.InternshipOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternshipOfferRepository extends JpaRepository<InternshipOffer, Long> {
} 