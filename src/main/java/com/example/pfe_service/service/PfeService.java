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
            Pfe pfe = pfeOptional.get();
            if (pfe.getRapportUrl() != null) {
                String presignedUrl = s3Service.generatePresignedUrl(pfe.getRapportUrl());
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
        return pfeList.stream()
                .map(pfe -> {
                    if (pfe.getRapportUrl() != null) {
                        String presignedUrl = s3Service.generatePresignedUrl(pfe.getRapportUrl());
                        pfe.setRapportUrl(presignedUrl);
                    }
                    return pfe;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Pfe updatePfe(Pfe pfe) {
        log.info("Updating Pfe: {}", pfe);
        if (pfeRepository.existsById(pfe.getId())) {
            Pfe existingPfe = pfeRepository.findById(pfe.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pfe not found"));
            pfe.setRapportUrl(existingPfe.getRapportUrl());
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
                .map(pfe -> {
                    if (pfe.getRapportUrl() != null) {
                        String presignedUrl = s3Service.generatePresignedUrl(pfe.getRapportUrl());
                        pfe.setRapportUrl(presignedUrl);
                    }
                    return pfe;
                })
                .collect(Collectors.toList());
    }
}