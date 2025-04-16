package com.example.pfe_service.controller;

import com.example.pfe_service.entities.SavedPfe;
import com.example.pfe_service.service.ISavedPfeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-pfes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SavedPfeController {
    private final ISavedPfeService savedPfeService;

    @PostMapping("/{pfeId}")
    public ResponseEntity<SavedPfe> savePfe(
            @PathVariable Long pfeId,
            @RequestHeader("X-Company-Id") String companyId) {
        SavedPfe savedPfe = savedPfeService.savePfe(companyId, pfeId);
        return new ResponseEntity<>(savedPfe, HttpStatus.CREATED);
    }

    @DeleteMapping("/{pfeId}")
    public ResponseEntity<Void> unsavePfe(
            @PathVariable Long pfeId,
            @RequestHeader("X-Company-Id") String companyId) {
        savedPfeService.unsavePfe(companyId, pfeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SavedPfe>> getSavedPfes(
            @RequestHeader("X-Company-Id") String companyId) {
        List<SavedPfe> savedPfes = savedPfeService.getSavedPfesByCompanyId(companyId);
        return ResponseEntity.ok(savedPfes);
    }

    @GetMapping("/{pfeId}/is-saved")
    public ResponseEntity<Map<String, Boolean>> isPfeSaved(
            @PathVariable Long pfeId,
            @RequestHeader("X-Company-Id") String companyId) {
        boolean isSaved = savedPfeService.isPfeSaved(companyId, pfeId);
        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }
} 