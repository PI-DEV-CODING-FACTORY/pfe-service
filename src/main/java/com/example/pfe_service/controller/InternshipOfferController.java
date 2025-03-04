package com.example.pfe_service.controller;

import com.example.pfe_service.entities.InternshipOffer;
import com.example.pfe_service.service.IInternshipOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internship-offers")
@RequiredArgsConstructor
public class InternshipOfferController {
    private final IInternshipOfferService internshipOfferService;

    @PostMapping
    public ResponseEntity<InternshipOffer> createInternshipOffer(@RequestBody InternshipOffer internshipOffer) {
        InternshipOffer createdOffer = internshipOfferService.createInternshipOffer(internshipOffer);
        return new ResponseEntity<>(createdOffer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InternshipOffer> updateInternshipOffer(
            @PathVariable Long id,
            @RequestBody InternshipOffer internshipOffer) {
        internshipOffer.setId(id);
        InternshipOffer updatedOffer = internshipOfferService.updateInternshipOffer(internshipOffer);
        return ResponseEntity.ok(updatedOffer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInternshipOffer(@PathVariable Long id) {
        internshipOfferService.deleteInternshipOffer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternshipOffer> getInternshipOfferById(@PathVariable Long id) {
        InternshipOffer internshipOffer = internshipOfferService.getInternshipOfferById(id);
        return ResponseEntity.ok(internshipOffer);
    }

    @GetMapping
    public ResponseEntity<List<InternshipOffer>> getAllInternshipOffers() {
        List<InternshipOffer> offers = internshipOfferService.getAllInternshipOffers();
        return ResponseEntity.ok(offers);
    }
} 