package com.example.pfe_service.repository;

import com.example.pfe_service.entities.SavedPfe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPfeRepository extends JpaRepository<SavedPfe, Long> {
    List<SavedPfe> findByCompanyId(String companyId);
    boolean existsByCompanyIdAndPfeId(String companyId, Long pfeId);
    void deleteByCompanyIdAndPfeId(String companyId, Long pfeId);
}
