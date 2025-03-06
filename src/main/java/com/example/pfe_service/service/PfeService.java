package com.example.pfe_service.service;

import com.example.pfe_service.dto.PfeCreateRequest;
import com.example.pfe_service.dto.PfeFilterRequest;
import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Technologies;
import com.example.pfe_service.repository.PfeRepository;
import com.example.pfe_service.repository.PfeSpecification;
import com.example.pfe_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PfeService implements IPfeService {
    private final PfeRepository pfeRepository;
    private final S3Service s3Service;

    @Override
    public Pfe createPfe(PfeCreateRequest request, MultipartFile rapport) {
        log.info("Creating new Pfe with title: {}", request.getTitle());
        
        String objectKey = s3Service.uploadFile(rapport);
        
        Pfe pfe = new Pfe();
        pfe.setTitle(request.getTitle());
        pfe.setDescription(request.getDescription());
        pfe.setGithubUrl(request.getGithubUrl());
        pfe.setVideoUrl(request.getVideoUrl());
        pfe.setTechnologies(request.getTechnologies());
        pfe.setOpenFor(request.getOpenFor());
        pfe.setStudentId(request.getStudentId());
        pfe.setRapportUrl(objectKey);
        pfe.setProcessing(true);
        
        return pfeRepository.save(pfe);
    }

    @Override
    public Pfe getPfeById(Long id) {
        log.info("Fetching Pfe by ID: {}", id);
        Optional<Pfe> pfeOptional = pfeRepository.findById(id);
        if (pfeOptional.isPresent()) {
            Pfe originalPfe = pfeOptional.get();
            
            // Create a deep copy to avoid modifying the entity that might be managed by JPA
            Pfe pfe = new Pfe();
            pfe.setId(originalPfe.getId());
            pfe.setTitle(originalPfe.getTitle());
            pfe.setDescription(originalPfe.getDescription());
            pfe.setGithubUrl(originalPfe.getGithubUrl());
            pfe.setVideoUrl(originalPfe.getVideoUrl());
            pfe.setTechnologies(originalPfe.getTechnologies());
            pfe.setOpenFor(originalPfe.getOpenFor());
            pfe.setStudentId(originalPfe.getStudentId());
            pfe.setProcessing(originalPfe.getProcessing());
            pfe.setCreatedAt(originalPfe.getCreatedAt());
            pfe.setUpdatedAt(originalPfe.getUpdatedAt());
            
            // Generate presigned URL only if rapportUrl exists
            if (originalPfe.getRapportUrl() != null) {
                String presignedUrl = s3Service.generatePresignedUrl(originalPfe.getRapportUrl());
                pfe.setRapportUrl(presignedUrl);
            }
            
            return pfe;
        } else {
            log.error("Pfe with ID {} not found", id);
            throw new ResourceNotFoundException("Pfe not found with ID: " + id);
        }
    }

    @Override
    public List<Pfe> getAllPfe() {
        log.info("Fetching all Pfe records");
        List<Pfe> pfeList = pfeRepository.findAll();
        return addPresignedUrls(pfeList);
    }

    @Override
    public Pfe updatePfe(Pfe pfe) {
        log.info("Updating Pfe: {}", pfe);
        if (pfeRepository.existsById(pfe.getId())) {
            // Get the existing PFE to preserve the original S3 object key
            Pfe existingPfe = pfeRepository.findById(pfe.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pfe not found"));
            
            // Check if the rapportUrl is a presigned URL (starts with https://pidev-2025.s3)
            if (pfe.getRapportUrl() != null && pfe.getRapportUrl().startsWith("https://pidev-2025.s3")) {
                // Keep the original S3 object key instead of the presigned URL
                pfe.setRapportUrl(existingPfe.getRapportUrl());
            }
            
            return pfeRepository.save(pfe);
        } else {
            log.error("Pfe with ID {} not found for update", pfe.getId());
            throw new ResourceNotFoundException("Pfe not found with ID: " + pfe.getId());
        }
    }

    @Override
    public void deletePfe(Long id) {
        log.info("Deleting Pfe with ID: {}", id);
        if (pfeRepository.existsById(id)) {
            Pfe pfe = pfeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pfe not found"));
            
            // Delete the rapport file from S3 if it exists
            if (pfe.getRapportUrl() != null) {
                s3Service.deleteFile(pfe.getRapportUrl());
            }
            
            pfeRepository.deleteById(id);
        } else {
            log.error("Pfe with ID {} not found for deletion", id);
            throw new ResourceNotFoundException("Pfe not found with ID: " + id);
        }
    }
    
    @Override
    public List<Pfe> findByTechnologies(List<Technologies> technologies) {
        log.info("Filtering PFEs by technologies: {}", technologies);
        List<Pfe> pfeList = pfeRepository.findByTechnologiesIn(technologies, technologies.size());
        return addPresignedUrls(pfeList);
    }
    
    @Override
    public List<Pfe> findByOpenFor(OpenFor openFor) {
        log.info("Filtering PFEs by openFor: {}", openFor);
        List<Pfe> pfeList = pfeRepository.findByOpenFor(openFor);
        return addPresignedUrls(pfeList);
    }
    
    @Override
    public List<Pfe> findByStudentId(String studentId) {
        log.info("Filtering PFEs by studentId: {}", studentId);
        List<Pfe> pfeList = pfeRepository.findByStudentId(studentId);
        return addPresignedUrls(pfeList);
    }
    
    @Override
    public List<Pfe> findByProcessing(Boolean processing) {
        log.info("Filtering PFEs by processing status: {}", processing);
        List<Pfe> pfeList = pfeRepository.findByProcessing(processing);
        return addPresignedUrls(pfeList);
    }
    
    @Override
    public List<Pfe> searchByKeyword(String keyword) {
        log.info("Searching PFEs by keyword: {}", keyword);
        List<Pfe> pfeList = pfeRepository.findByTitleOrDescriptionContaining(keyword);
        return addPresignedUrls(pfeList);
    }
    
    @Override
    public List<Pfe> filterPfes(PfeFilterRequest filterRequest) {
        log.info("Filtering PFEs with advanced criteria: {}", filterRequest);
        List<Pfe> pfeList = pfeRepository.findAll(PfeSpecification.filterBy(filterRequest));
        return addPresignedUrls(pfeList);
    }
    
    // Helper method to add presigned URLs to rapport files
    private List<Pfe> addPresignedUrls(List<Pfe> pfeList) {
        return pfeList.stream()
                .map(originalPfe -> {
                    // Create a deep copy to avoid modifying the entity that might be managed by JPA
                    Pfe pfe = new Pfe();
                    pfe.setId(originalPfe.getId());
                    pfe.setTitle(originalPfe.getTitle());
                    pfe.setDescription(originalPfe.getDescription());
                    pfe.setGithubUrl(originalPfe.getGithubUrl());
                    pfe.setVideoUrl(originalPfe.getVideoUrl());
                    pfe.setTechnologies(originalPfe.getTechnologies());
                    pfe.setOpenFor(originalPfe.getOpenFor());
                    pfe.setStudentId(originalPfe.getStudentId());
                    pfe.setProcessing(originalPfe.getProcessing());
                    pfe.setCreatedAt(originalPfe.getCreatedAt());
                    pfe.setUpdatedAt(originalPfe.getUpdatedAt());
                    
                    // Generate presigned URL only if rapportUrl exists
                    if (originalPfe.getRapportUrl() != null) {
                        String presignedUrl = s3Service.generatePresignedUrl(originalPfe.getRapportUrl());
                        pfe.setRapportUrl(presignedUrl);
                    } else {
                        pfe.setRapportUrl(null);
                    }
                    
                    return pfe;
                })
                .collect(Collectors.toList());
    }
}