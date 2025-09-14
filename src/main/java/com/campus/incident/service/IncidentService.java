package com.campus.incident.service;

import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IncidentService {
    
    // Core CRUD operations
    IncidentReport createIncident(IncidentReport incident, User reporter);
    
    IncidentReport getIncidentById(Long id);
    
    IncidentReport updateIncident(Long id, IncidentReport incidentDetails, User updater);
    
    void deleteIncident(Long id, User deleter);
    
    // Status management
    IncidentReport updateIncidentStatus(Long incidentId, IncidentStatus newStatus, User updater);
    
    IncidentReport assignIncident(Long incidentId, Long assigneeId, User assigner);
    
    IncidentReport startWork(Long incidentId, User worker);
    
    IncidentReport pauseWork(Long incidentId, User worker, String reason);
    
    IncidentReport completeWork(Long incidentId, User worker, String resolutionNotes);
    
    IncidentReport closeIncident(Long incidentId, User closer, String closureNotes);
    
    // Resolution logging
    void addResolutionLog(Long incidentId, String action, String notes, User performer);
    
    void addTimeLog(Long incidentId, Integer minutesSpent, String notes, User performer);
    
    void addCostLog(Long incidentId, Double cost, String description, User performer);
    
    void addMaterialLog(Long incidentId, String materials, String notes, User performer);
    
    // Search and filtering
    Page<IncidentReport> getIncidentsWithFilters(Pageable pageable, IncidentStatus status, Long categoryId, 
                                                Long reporterId, Long assignedToId, Integer priorityLevel, 
                                                Boolean isUrgent, String search, User currentUser);
    
    Page<IncidentReport> searchIncidents(String searchTerm, Pageable pageable);
    
    Page<IncidentReport> searchIncidentsByStatus(String searchTerm, List<IncidentStatus> statuses, Pageable pageable);
    
    List<IncidentReport> getIncidentsByStatus(IncidentStatus status);
    
    List<IncidentReport> getIncidentsByCategory(Long categoryId);
    
    List<IncidentReport> getIncidentsByReporter(Long reporterId);
    
    List<IncidentReport> getIncidentsByAssignee(Long assigneeId);
    
    List<IncidentReport> getIncidentsByPriority(Integer minPriority);
    
    List<IncidentReport> getUrgentIncidents();
    
    List<IncidentReport> getOverdueIncidents();
    
    // Dashboard and reporting
    Map<String, Object> getDashboardStatistics();
    
    Map<String, Long> getIncidentCountByStatus();
    
    Map<String, Long> getIncidentCountByCategory();
    
    Map<String, Long> getIncidentCountByPriority();
    
    List<IncidentReport> getRecentIncidents(int limit);
    
    List<IncidentReport> getPendingIncidents();
    
    List<IncidentReport> getActiveIncidents();
    
    // Business logic
    boolean canUserViewIncident(IncidentReport incident, User user);
    
    boolean canUserUpdateIncident(IncidentReport incident, User user);
    
    boolean canUserDeleteIncident(IncidentReport incident, User user);
    
    List<IncidentStatus> getAvailableStatusTransitions(IncidentReport incident, User user);
    
    // Notifications and alerts
    void sendStatusUpdateNotification(IncidentReport incident, IncidentStatus oldStatus, IncidentStatus newStatus);
    
    void sendAssignmentNotification(IncidentReport incident, User assignee);
    
    void sendOverdueAlert(IncidentReport incident);
    
    // Bulk operations
    List<IncidentReport> bulkUpdateStatus(List<Long> incidentIds, IncidentStatus newStatus, User updater, String notes);
    
    List<IncidentReport> bulkAssign(List<Long> incidentIds, Long assigneeId, User assigner);
    
    // Export and reporting
    byte[] exportIncidentsToCSV(List<IncidentReport> incidents);
    
    byte[] exportIncidentsToPDF(List<IncidentReport> incidents);
    
    String generateIncidentReport(Long incidentId);
}
