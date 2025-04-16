package com.example.pfe_service.service;

import com.example.pfe_service.entities.SavedPfe;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.repository.SavedPfeRepository;
import com.example.pfe_service.repository.PfeRepository;
import com.example.pfe_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SavedPfeService implements ISavedPfeService {
    private final SavedPfeRepository savedPfeRepository;
    private final PfeRepository pfeRepository;

    @Override
    public SavedPfe savePfe(String companyId, Long pfeId) {
        log.info("Saving PFE {} for company {}", pfeId, companyId);
        
        // Check if PFE exists
        Pfe pfe = pfeRepository.findById(pfeId)
                .orElseThrow(() -> new ResourceNotFoundException("PFE not found with id: " + pfeId));
        
        // Check if already saved
        if (savedPfeRepository.existsByCompanyIdAndPfeId(companyId, pfeId)) {
            throw new RuntimeException("PFE is already saved by this company");
        }
        
        SavedPfe savedPfe = new SavedPfe();
        savedPfe.setCompanyId(companyId);
        savedPfe.setPfe(pfe);
        
        return savedPfeRepository.save(savedPfe);
    }

    @Override
    public void unsavePfe(String companyId, Long pfeId) {
        log.info("Unsaving PFE {} for company {}", pfeId, companyId);
        savedPfeRepository.deleteByCompanyIdAndPfeId(companyId, pfeId);
    }

    @Override
    public List<SavedPfe> getSavedPfesByCompanyId(String companyId) {
        log.info("Fetching saved PFEs for company {}", companyId);
        return savedPfeRepository.findByCompanyId(companyId);
    }

    @Override
    public boolean isPfeSaved(String companyId, Long pfeId) {
        return savedPfeRepository.existsByCompanyIdAndPfeId(companyId, pfeId);
    }
} 