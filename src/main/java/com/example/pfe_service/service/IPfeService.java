package com.example.pfe_service.service;

import com.example.pfe_service.dto.PfeCreateRequest;
import com.example.pfe_service.dto.PfeFilterRequest;
import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Technologies;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPfeService {
    // Create a new Pfe with file upload
    Pfe createPfe(PfeCreateRequest request, MultipartFile rapport);

    // Retrieve a Pfe entity by its ID
    Pfe getPfeById(Long id);

    // Retrieve all Pfe entities
    List<Pfe> getAllPfe();

    // Update an existing Pfe entity
    Pfe updatePfe(Pfe pfe);

    // Delete a Pfe entity by its ID
    void deletePfe(Long id);
    
    // Filter Pfe entities by technologies
    List<Pfe> findByTechnologies(List<Technologies> technologies);
    
    // Filter Pfe entities by openFor
    List<Pfe> findByOpenFor(OpenFor openFor);
    
    // Filter Pfe entities by studentId
    List<Pfe> findByStudentId(String studentId);
    
    // Filter Pfe entities by processing status
    List<Pfe> findByProcessing(Boolean processing);
    
    // Search Pfe entities by keyword in title or description
    List<Pfe> searchByKeyword(String keyword);
    
    // Advanced filtering of Pfe entities
    List<Pfe> filterPfes(PfeFilterRequest filterRequest);
}