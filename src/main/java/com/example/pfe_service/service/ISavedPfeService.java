package com.example.pfe_service.service;

import com.example.pfe_service.entities.SavedPfe;
import java.util.List;

public interface ISavedPfeService {
    SavedPfe savePfe(String companyId, Long pfeId);
    void unsavePfe(String companyId, Long pfeId);
    List<SavedPfe> getSavedPfesByCompanyId(String companyId);
    boolean isPfeSaved(String companyId, Long pfeId);
} 