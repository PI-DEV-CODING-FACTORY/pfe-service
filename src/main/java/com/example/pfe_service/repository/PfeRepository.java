package com.example.pfe_service.repository;

import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Technologies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PfeRepository extends JpaRepository<Pfe, Long>, JpaSpecificationExecutor<Pfe> {
    
    @Query("SELECT p FROM Pfe p JOIN p.technologies t WHERE t IN :technologies GROUP BY p HAVING COUNT(DISTINCT t) = :count")
    List<Pfe> findByTechnologiesIn(@Param("technologies") List<Technologies> technologies, @Param("count") long count);
    
    List<Pfe> findByOpenFor(OpenFor openFor);
    
    List<Pfe> findByStudentId(String studentId);
    
    List<Pfe> findByProcessing(Boolean processing);
    
    @Query("SELECT p FROM Pfe p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Pfe> findByTitleOrDescriptionContaining(@Param("keyword") String keyword);
}
