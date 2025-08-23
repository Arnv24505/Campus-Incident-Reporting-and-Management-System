package com.campus.incident.repository;

import com.campus.incident.entity.IncidentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, Long> {
    
    Optional<IncidentCategory> findByName(String name);
    
    List<IncidentCategory> findByIsActive(boolean isActive);
    
    List<IncidentCategory> findByPriorityLevel(Integer priorityLevel);
    
    List<IncidentCategory> findByPriorityLevelGreaterThanEqual(Integer minPriority);
    
    @Query("SELECT ic FROM IncidentCategory ic WHERE ic.name LIKE %:searchTerm% OR ic.description LIKE %:searchTerm%")
    List<IncidentCategory> searchCategories(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT ic FROM IncidentCategory ic WHERE ic.priorityLevel >= :minPriority AND ic.isActive = true ORDER BY ic.priorityLevel DESC")
    List<IncidentCategory> findHighPriorityActiveCategories(@Param("minPriority") Integer minPriority);
    
    @Query("SELECT COUNT(ic) FROM IncidentCategory ic WHERE ic.isActive = true")
    long countActiveCategories();
    
    @Query("SELECT ic.priorityLevel, COUNT(ic) FROM IncidentCategory ic GROUP BY ic.priorityLevel ORDER BY ic.priorityLevel DESC")
    List<Object[]> getCategoryCountByPriority();
}
