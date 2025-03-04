package com.example.pfe_service.controller;

import com.example.pfe_service.dto.PfeCreateRequest;
import com.example.pfe_service.dto.PfeFilterRequest;
import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Technologies;
import com.example.pfe_service.service.IPfeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/api/pfe")
@RestController
@CrossOrigin("*")
public class PfeController {
    private final IPfeService pfeService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Pfe> createPfe(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("githubUrl") String githubUrl,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "technologies", required = false) String technologiesStr,
            @RequestParam("openFor") String openForStr,
            @RequestParam("studentId") String studentId,
            @RequestParam("rapport") MultipartFile rapport) {
        try {
            if (!rapport.getContentType().equals("application/pdf")) {
                throw new RuntimeException("Only PDF files are allowed");
            }
            
            // Parse technologies from comma-separated string if provided
            List<Technologies> technologies = new ArrayList<>();
            if (technologiesStr != null && !technologiesStr.isEmpty()) {
                technologies = Arrays.stream(technologiesStr.split(","))
                        .map(tech -> Technologies.valueOf(tech.trim()))
                        .collect(Collectors.toList());
            }
            
            // Parse openFor enum
            OpenFor openFor = OpenFor.valueOf(openForStr.trim());
            
            // Create PfeCreateRequest object
            PfeCreateRequest request = new PfeCreateRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setGithubUrl(githubUrl);
            request.setVideoUrl(videoUrl);
            request.setTechnologies(technologies);
            request.setOpenFor(openFor);
            request.setStudentId(studentId);
            
            Pfe newPfe = pfeService.createPfe(request, rapport);
            return new ResponseEntity<>(newPfe, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Error processing the request: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pfe> getPfeById(@PathVariable Long id) {
        Pfe pfe = pfeService.getPfeById(id);
        return ResponseEntity.ok(pfe);
    }

    @GetMapping
    public ResponseEntity<List<Pfe>> getAllPfe() {
        List<Pfe> pfeList = pfeService.getAllPfe();
        return ResponseEntity.ok(pfeList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pfe> updatePfe(@PathVariable Long id, @RequestBody Pfe pfe) {
        pfe.setId(id);
        Pfe updatedPfe = pfeService.updatePfe(pfe);
        return ResponseEntity.ok(updatedPfe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePfe(@PathVariable Long id) {
        pfeService.deletePfe(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<Pfe>> filterPfes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Technologies> technologies,
            @RequestParam(required = false) OpenFor openFor,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Boolean processing,
            @RequestParam(required = false) String createdAfter,
            @RequestParam(required = false) String createdBefore,
            @RequestParam(required = false) String updatedAfter,
            @RequestParam(required = false) String updatedBefore) {
        
        PfeFilterRequest filterRequest = new PfeFilterRequest();
        
        // Set search keyword for title and description
        if (keyword != null && !keyword.isEmpty()) {
            filterRequest.setTitleContains(keyword);
            filterRequest.setDescriptionContains(keyword);
        }
        
        // Set technologies filter
        if (technologies != null && !technologies.isEmpty()) {
            filterRequest.setTechnologies(technologies);
        }
        
        // Set openFor filter
        if (openFor != null) {
            filterRequest.setOpenFor(openFor);
        }
        
        // Set studentId filter
        if (studentId != null && !studentId.isEmpty()) {
            filterRequest.setStudentId(studentId);
        }
        
        // Set processing status filter
        if (processing != null) {
            filterRequest.setProcessing(processing);
        }
        
        // Parse and set date filters if provided
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        if (createdAfter != null && !createdAfter.isEmpty()) {
            filterRequest.setCreatedAfter(LocalDateTime.parse(createdAfter, formatter));
        }
        
        if (createdBefore != null && !createdBefore.isEmpty()) {
            filterRequest.setCreatedBefore(LocalDateTime.parse(createdBefore, formatter));
        }
        
        if (updatedAfter != null && !updatedAfter.isEmpty()) {
            filterRequest.setUpdatedAfter(LocalDateTime.parse(updatedAfter, formatter));
        }
        
        if (updatedBefore != null && !updatedBefore.isEmpty()) {
            filterRequest.setUpdatedBefore(LocalDateTime.parse(updatedBefore, formatter));
        }
        
        List<Pfe> pfeList = pfeService.filterPfes(filterRequest);
        return ResponseEntity.ok(pfeList);
    }
}