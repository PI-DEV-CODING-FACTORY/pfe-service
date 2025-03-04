package com.example.pfe_service.service;

import com.example.pfe_service.entities.InternshipOffer;
import com.example.pfe_service.repository.InternshipOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipOfferService implements IInternshipOfferService {
    private final InternshipOfferRepository internshipOfferRepository;

    @Override
    public InternshipOffer createInternshipOffer(InternshipOffer internshipOffer) {
        log.info("Creating new internship offer");
        return internshipOfferRepository.save(internshipOffer);
    }

    @Override
    public InternshipOffer updateInternshipOffer(InternshipOffer internshipOffer) {
        log.info("Updating internship offer with id: {}", internshipOffer.getId());
        if (!internshipOfferRepository.existsById(internshipOffer.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Internship offer not found");
        }
        return internshipOfferRepository.save(internshipOffer);
    }

    @Override
    public void deleteInternshipOffer(Long id) {
        log.info("Deleting internship offer with id: {}", id);
        if (!internshipOfferRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Internship offer not found");
        }
        internshipOfferRepository.deleteById(id);
    }

    @Override
    public InternshipOffer getInternshipOfferById(Long id) {
        log.info("Fetching internship offer with id: {}", id);
        return internshipOfferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Internship offer not found"));
    }

    @Override
    public List<InternshipOffer> getAllInternshipOffers() {
        log.info("Fetching all internship offers");
        return internshipOfferRepository.findAll();
    }
} 