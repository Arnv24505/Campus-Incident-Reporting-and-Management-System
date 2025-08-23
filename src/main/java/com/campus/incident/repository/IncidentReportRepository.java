package com.campus.incident.repository;

import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.IncidentCategory;
import com.campus.incident.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    
    // Basic queries
    List<IncidentReport> findByStatus(IncidentStatus status);
    
    List<IncidentReport> findByCategory(IncidentCategory category);
    
    List<IncidentReport> findByCategoryId(Long categoryId);
    
    List<IncidentReport> findByReporter(User reporter);
    
    Page<IncidentReport> findByReporter(User reporter, Pageable pageable);
    
    List<IncidentReport> findByAssignedTo(User assignedTo);
    
    Page<IncidentReport> findByAssignedTo(User assignedTo, Pageable pageable);
    
    Page<IncidentReport> findByStatusInAndAssignedToIsNull(List<IncidentStatus> statuses, Pageable pageable);
    
    List<IncidentReport> findByPriorityLevel(Integer priorityLevel);
    
    List<IncidentReport> findByIsUrgent(boolean isUrgent);
    
    // Complex queries
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.status IN :statuses")
    List<IncidentReport> findByStatusIn(@Param("statuses") List<IncidentStatus> statuses);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.category.id = :categoryId AND ir.status IN :statuses")
    List<IncidentReport> findByCategoryAndStatusIn(@Param("categoryId") Long categoryId, @Param("statuses") List<IncidentStatus> statuses);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.reporter.id = :reporterId AND ir.status IN :statuses")
    List<IncidentReport> findByReporterAndStatusIn(@Param("reporterId") Long reporterId, @Param("statuses") List<IncidentStatus> statuses);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.assignedTo.id = :assignedToId AND ir.status IN :statuses")
    List<IncidentReport> findByAssignedToAndStatusIn(@Param("assignedToId") Long assignedToId, @Param("statuses") List<IncidentStatus> statuses);
    
    // Search queries
    @Query("SELECT ir FROM IncidentReport ir WHERE " +
           "ir.title LIKE %:searchTerm% OR " +
           "ir.description LIKE %:searchTerm% OR " +
           "ir.locationDetails LIKE %:searchTerm%")
    Page<IncidentReport> searchIncidents(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE " +
           "ir.title LIKE %:searchTerm% OR " +
           "ir.description LIKE %:searchTerm% OR " +
           "ir.locationDetails LIKE %:searchTerm% AND " +
           "ir.status IN :statuses")
    Page<IncidentReport> searchIncidentsByStatus(@Param("searchTerm") String searchTerm, 
                                                 @Param("statuses") List<IncidentStatus> statuses, 
                                                 Pageable pageable);
    
    // Date-based queries
    List<IncidentReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IncidentReport> findByCreatedAtAfter(LocalDateTime date);
    
    List<IncidentReport> findByUpdatedAtAfter(LocalDateTime date);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.estimatedResolutionDate < :currentDate AND ir.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<IncidentReport> findOverdueIncidents(@Param("currentDate") LocalDateTime currentDate);
    
    // Priority and urgency queries
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.priorityLevel >= :minPriority OR ir.isUrgent = true")
    List<IncidentReport> findHighPriorityIncidents(@Param("minPriority") Integer minPriority);
    
    List<IncidentReport> findByPriorityLevelGreaterThanEqualAndStatusIn(Integer priorityLevel, List<IncidentStatus> statuses);
    
    // Statistics queries
    @Query("SELECT COUNT(ir) FROM IncidentReport ir WHERE ir.status = :status")
    long countByStatus(@Param("status") IncidentStatus status);
    
    @Query("SELECT COUNT(ir) FROM IncidentReport ir WHERE ir.category.id = :categoryId")
    long countByCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(ir) FROM IncidentReport ir WHERE ir.reporter.id = :reporterId")
    long countByReporter(@Param("reporterId") Long reporterId);
    
    @Query("SELECT COUNT(ir) FROM IncidentReport ir WHERE ir.assignedTo.id = :assignedToId AND ir.status IN ('ASSIGNED', 'IN_PROGRESS')")
    long countActiveAssignmentsByUser(@Param("assignedToId") Long assignedToId);
    
    // Dashboard queries
    @Query("SELECT ir.status, COUNT(ir) FROM IncidentReport ir GROUP BY ir.status")
    List<Object[]> getIncidentCountByStatus();
    
    @Query("SELECT ir.category.name, COUNT(ir) FROM IncidentReport ir GROUP BY ir.category.name")
    List<Object[]> getIncidentCountByCategory();
    
    @Query("SELECT ir.priorityLevel, COUNT(ir) FROM IncidentReport ir GROUP BY ir.priorityLevel ORDER BY ir.priorityLevel DESC")
    List<Object[]> getIncidentCountByPriority();
    
    // Recent activity
    @Query("SELECT ir FROM IncidentReport ir ORDER BY ir.updatedAt DESC")
    Page<IncidentReport> findRecentIncidents(Pageable pageable);
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.status IN ('REPORTED', 'UNDER_REVIEW') ORDER BY ir.createdAt ASC")
    List<IncidentReport> findPendingIncidents();
    
    @Query("SELECT ir FROM IncidentReport ir WHERE ir.status IN ('ASSIGNED', 'IN_PROGRESS') ORDER BY ir.priorityLevel DESC, ir.createdAt ASC")
    List<IncidentReport> findActiveIncidents();
}
