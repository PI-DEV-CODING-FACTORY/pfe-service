package com.example.pfe_service.repository;

import com.example.pfe_service.dto.PfeFilterRequest;
import com.example.pfe_service.entities.OpenFor;
import com.example.pfe_service.entities.Pfe;
import com.example.pfe_service.entities.Technologies;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PfeSpecification {

    public static Specification<Pfe> filterBy(PfeFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by title
            if (filter.getTitleContains() != null && !filter.getTitleContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    "%" + filter.getTitleContains().toLowerCase() + "%"
                ));
            }
            
            // Filter by description
            if (filter.getDescriptionContains() != null && !filter.getDescriptionContains().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    "%" + filter.getDescriptionContains().toLowerCase() + "%"
                ));
            }
            
            // Filter by technologies
            if (filter.getTechnologies() != null && !filter.getTechnologies().isEmpty()) {
                Join<Pfe, Technologies> technologiesJoin = root.join("technologies");
                predicates.add(technologiesJoin.in(filter.getTechnologies()));
                
                // Use distinct to avoid duplicates
                query.distinct(true);
            }
            
            // Filter by openFor
            if (filter.getOpenFor() != null) {
                predicates.add(criteriaBuilder.equal(root.get("openFor"), filter.getOpenFor()));
            }
            
            // Filter by studentId
            if (filter.getStudentId() != null && !filter.getStudentId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("studentId"), filter.getStudentId()));
            }
            
            // Filter by processing status
            if (filter.getProcessing() != null) {
                predicates.add(criteriaBuilder.equal(root.get("processing"), filter.getProcessing()));
            }
            
            // Filter by creation date range
            if (filter.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }
            
            if (filter.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
            }
            
            // Filter by update date range
            if (filter.getUpdatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAfter()));
            }
            
            if (filter.getUpdatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedBefore()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
} 