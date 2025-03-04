package com.example.pfe_service.service;

import com.example.pfe_service.entities.InternshipOffer;
import java.util.List;

public interface IInternshipOfferService {
    InternshipOffer createInternshipOffer(InternshipOffer internshipOffer);
    InternshipOffer updateInternshipOffer(InternshipOffer internshipOffer);
    void deleteInternshipOffer(Long id);
    InternshipOffer getInternshipOfferById(Long id);
    List<InternshipOffer> getAllInternshipOffers();
} 