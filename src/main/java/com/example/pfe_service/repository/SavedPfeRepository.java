package com.example.pfe_service.repository;

import com.example.pfe_service.entities.SavedPfe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPfeRepository extends JpaRepository<SavedPfe, Long> {

}
