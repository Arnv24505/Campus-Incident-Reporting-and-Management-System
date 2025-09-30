package com.campus.incident.service.impl;

import com.campus.incident.entity.*;
import com.campus.incident.repository.IncidentReportRepository;
import com.campus.incident.repository.UserRepository;
import com.campus.incident.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncidentServiceImpl implements IncidentService {
    
    @Autowired
    private IncidentReportRepository incidentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public IncidentReport createIncident(IncidentReport incident, User reporter) {
        // Set initial values
        incident.setReporter(reporter);
        incident.setStatus(IncidentStatus.REPORTED);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Set default values if not provided
        if (incident.getTitle() == null || incident.getTitle().trim().isEmpty()) {
            incident.setTitle("Untitled Incident");
        }
        if (incident.getDescription() == null || incident.getDescription().trim().isEmpty()) {
            incident.setDescription("No description provided");
        }
        if (incident.getPriorityLevel() == null) {
            incident.setPriorityLevel(1);
        }
        
        // Set priority based on category if not specified
        if (incident.getPriorityLevel() == null && incident.getCategory() != null) {
            incident.setPriorityLevel(incident.getCategory().getPriorityLevel());
        }
        
        // Save incident
        IncidentReport savedIncident = incidentRepository.save(incident);
        
        // Add initial status update
        savedIncident.addResolutionLog("Incident reported", "Initial incident report created", reporter);
        
        return savedIncident;
    }
    
    @Override
    public IncidentReport getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));
    }
    
    @Override
    public IncidentReport updateIncident(Long id, IncidentReport incidentDetails, User updater) {
        IncidentReport existingIncident = getIncidentById(id);
        
        // Check permissions
        if (!canUserUpdateIncident(existingIncident, updater)) {
            throw new RuntimeException("User not authorized to update this incident");
        }
        
        // Update allowed fields
        if (incidentDetails.getTitle() != null) {
            existingIncident.setTitle(incidentDetails.getTitle());
        }
        if (incidentDetails.getDescription() != null) {
            existingIncident.setDescription(incidentDetails.getDescription());
        }
        if (incidentDetails.getLocationDetails() != null) {
            existingIncident.setLocationDetails(incidentDetails.getLocationDetails());
        }
        if (incidentDetails.getCategory() != null) {
            existingIncident.setCategory(incidentDetails.getCategory());
        }
        if (incidentDetails.getPriorityLevel() != null) {
            existingIncident.setPriorityLevel(incidentDetails.getPriorityLevel());
        }
        if (incidentDetails.getEstimatedResolutionDate() != null) {
            existingIncident.setEstimatedResolutionDate(incidentDetails.getEstimatedResolutionDate());
        }
        if (incidentDetails.isUrgent() != existingIncident.isUrgent()) {
            existingIncident.setUrgent(incidentDetails.isUrgent());
        }
        if (incidentDetails.isConfidential() != existingIncident.isConfidential()) {
            existingIncident.setConfidential(incidentDetails.isConfidential());
        }
        
        existingIncident.setUpdatedAt(LocalDateTime.now());
        
        // Add update log
        existingIncident.addResolutionLog("Incident updated", "Incident details modified by " + updater.getUsername(), updater);
        
        return incidentRepository.save(existingIncident);
    }
    
    @Override
    public void deleteIncident(Long id, User deleter) {
        IncidentReport incident = getIncidentById(id);
        
        if (!canUserDeleteIncident(incident, deleter)) {
            throw new RuntimeException("User not authorized to delete this incident");
        }
        
        // Only allow deletion of reported incidents
        if (incident.getStatus() != IncidentStatus.REPORTED) {
            throw new RuntimeException("Cannot delete incident that is not in REPORTED status");
        }
        
        incidentRepository.delete(incident);
    }

    @Override
    public IncidentReport updateIncidentStatus(Long incidentId, IncidentStatus newStatus, User updater) {
        return updateIncidentStatus(incidentId, newStatus, updater, null);
    }

    @Override
    public IncidentReport updateIncidentStatus(Long incidentId, IncidentStatus newStatus, User updater, String notes) {
        IncidentReport incident = getIncidentById(incidentId);
        
        if (!canUserUpdateIncident(incident, updater)) {
            throw new RuntimeException("User not authorized to update this incident");
        }
        
        if (!incident.canTransitionTo(newStatus)) {
            throw new RuntimeException("Invalid status transition from " + incident.getStatus() + " to " + newStatus);
        }
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.updateStatus(newStatus, updater, notes);
        incident.setUpdatedAt(LocalDateTime.now());

        // Add resolution log
        incident.addResolutionLog("Status updated",
                "Status changed from " + oldStatus.getDisplayName() + " to " + newStatus.getDisplayName(), updater);

        IncidentReport savedIncident = incidentRepository.save(incident);
        
        // Send notifications
        sendStatusUpdateNotification(savedIncident, oldStatus, newStatus);
        
        return savedIncident;
    }
    
    @Override
    public IncidentReport assignIncident(Long incidentId, Long assigneeId, User assigner) {
        IncidentReport incident = getIncidentById(incidentId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));
        
        if (!canUserUpdateIncident(incident, assigner)) {
            throw new RuntimeException("User not authorized to assign this incident");
        }
        
        if (!assignee.getRole().isMaintenance()) {
            throw new RuntimeException("Assignee must have maintenance role");
        }
        
        incident.assignTo(assignee);
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Add assignment log
        incident.addResolutionLog("Incident assigned", 
                "Incident assigned to " + assignee.getUsername(), assigner);
        
        IncidentReport savedIncident = incidentRepository.save(incident);
        
        // Send assignment notification
        sendAssignmentNotification(savedIncident, assignee);
        
        return savedIncident;
    }
    
    @Override
    public IncidentReport startWork(Long incidentId, User worker) {
        IncidentReport incident = getIncidentById(incidentId);
        
        if (!incident.getAssignedTo().equals(worker)) {
            throw new RuntimeException("Only assigned worker can start work on this incident");
        }
        
        if (incident.getStatus() != IncidentStatus.ASSIGNED) {
            throw new RuntimeException("Incident must be in ASSIGNED status to start work");
        }
        
        incident.updateStatus(IncidentStatus.IN_PROGRESS, worker, "Work started");
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Add work start log
        incident.addResolutionLog("Work started", "Maintenance work has begun", worker);
        
        return incidentRepository.save(incident);
    }
    
    @Override
    public IncidentReport pauseWork(Long incidentId, User worker, String reason) {
        IncidentReport incident = getIncidentById(incidentId);
        
        if (!incident.getAssignedTo().equals(worker)) {
            throw new RuntimeException("Only assigned worker can pause work on this incident");
        }
        
        if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {
            throw new RuntimeException("Incident must be in IN_PROGRESS status to pause work");
        }
        
        incident.updateStatus(IncidentStatus.ON_HOLD, worker, reason);
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Add pause log
        incident.addResolutionLog("Work paused", "Work put on hold: " + reason, worker);
        
        return incidentRepository.save(incident);
    }
    
    @Override
    public IncidentReport completeWork(Long incidentId, User worker, String resolutionNotes) {
        IncidentReport incident = getIncidentById(incidentId);
        
        if (!incident.getAssignedTo().equals(worker)) {
            throw new RuntimeException("Only assigned worker can complete work on this incident");
        }
        
        if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {
            throw new RuntimeException("Incident must be in IN_PROGRESS status to complete work");
        }
        
        incident.updateStatus(IncidentStatus.RESOLVED, worker, resolutionNotes);
        incident.setActualResolutionDate(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Add completion log
        incident.addResolutionLog("Work completed", "Issue resolved: " + resolutionNotes, worker);
        
        return incidentRepository.save(incident);
    }
    
    @Override
    public IncidentReport closeIncident(Long incidentId, User closer, String closureNotes) {
        IncidentReport incident = getIncidentById(incidentId);
        
        if (!canUserUpdateIncident(incident, closer)) {
            throw new RuntimeException("User not authorized to close this incident");
        }
        
        if (incident.getStatus() != IncidentStatus.RESOLVED) {
            throw new RuntimeException("Incident must be in RESOLVED status to close");
        }
        
        incident.updateStatus(IncidentStatus.CLOSED, closer, closureNotes);
        incident.setUpdatedAt(LocalDateTime.now());
        
        // Add closure log
        incident.addResolutionLog("Incident closed", "Incident officially closed: " + closureNotes, closer);
        
        return incidentRepository.save(incident);
    }
    
    @Override
    public void addResolutionLog(Long incidentId, String action, String notes, User performer) {
        IncidentReport incident = getIncidentById(incidentId);
        incident.addResolutionLog(action, notes, performer);
        incidentRepository.save(incident);
    }
    
    @Override
    public void addTimeLog(Long incidentId, Integer minutesSpent, String notes, User performer) {
        IncidentReport incident = getIncidentById(incidentId);
        
        ResolutionLog timeLog = new ResolutionLog("Time logged", notes, performer);
        timeLog.setTimeSpentMinutes(minutesSpent);
        timeLog.setLogType(ResolutionLog.LogType.TIME_LOG);
        
        incident.getResolutionLogs().add(timeLog);
        incidentRepository.save(incident);
    }
    
    @Override
    public void addCostLog(Long incidentId, Double cost, String description, User performer) {
        IncidentReport incident = getIncidentById(incidentId);
        
        ResolutionLog costLog = new ResolutionLog("Cost logged", description, performer);
        costLog.setCostIncurred(cost);
        costLog.setLogType(ResolutionLog.LogType.COST_LOG);
        
        incident.getResolutionLogs().add(costLog);
        incidentRepository.save(incident);
    }
    
    @Override
    public void addMaterialLog(Long incidentId, String materials, String notes, User performer) {
        IncidentReport incident = getIncidentById(incidentId);
        
        ResolutionLog materialLog = new ResolutionLog("Materials used", notes, performer);
        materialLog.setMaterialsUsed(materials);
        materialLog.setLogType(ResolutionLog.LogType.MATERIAL_LOG);
        
        incident.getResolutionLogs().add(materialLog);
        incidentRepository.save(incident);
    }
    
    @Override
    public Page<IncidentReport> getIncidentsWithFilters(Pageable pageable, IncidentStatus status, Long categoryId, 
                                                       Long reporterId, Long assignedToId, Integer priorityLevel, 
                                                       Boolean isUrgent, String search, User currentUser) {
        
        // Apply role-based filtering
        if (currentUser.getRole() == UserRole.REPORTER) {
            // Reporters can only see their own incidents
            return incidentRepository.findByReporter(currentUser, pageable);
        } else if (currentUser.getRole() == UserRole.MAINTENANCE) {
            // Maintenance can see assigned incidents and available ones
            if (assignedToId != null && assignedToId.equals(currentUser.getId())) {
                return incidentRepository.findByAssignedTo(currentUser, pageable);
            } else {
                // Show incidents that can be assigned to this user
                return incidentRepository.findByStatusInAndAssignedToIsNull(
                    Arrays.asList(IncidentStatus.REPORTED, IncidentStatus.UNDER_REVIEW), pageable);
            }
        } else {
            // Admin can see all incidents with full filtering
            return incidentRepository.findAll(pageable);
        }
    }
    
    @Override
    public Page<IncidentReport> searchIncidents(String searchTerm, Pageable pageable) {
        return incidentRepository.searchIncidents(searchTerm, pageable);
    }
    
    @Override
    public Page<IncidentReport> searchIncidentsByStatus(String searchTerm, List<IncidentStatus> statuses, Pageable pageable) {
        return incidentRepository.searchIncidentsByStatus(searchTerm, statuses, pageable);
    }
    
    @Override
    public List<IncidentReport> getIncidentsByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status);
    }
    
    @Override
    public List<IncidentReport> getIncidentsByCategory(Long categoryId) {
        if (categoryId == null) {
            return new ArrayList<>();
        }
        return incidentRepository.findByCategoryId(categoryId);
    }
    
    @Override
    public List<IncidentReport> getIncidentsByReporter(Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));
        return incidentRepository.findByReporter(reporter);
    }
    
    @Override
    public List<IncidentReport> getIncidentsByAssignee(Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));
        return incidentRepository.findByAssignedTo(assignee);
    }
    
    @Override
    public List<IncidentReport> getIncidentsByPriority(Integer minPriority) {
        return incidentRepository.findByPriorityLevelGreaterThanEqualAndStatusIn(minPriority, 
                Arrays.asList(IncidentStatus.REPORTED, IncidentStatus.UNDER_REVIEW, IncidentStatus.ASSIGNED, IncidentStatus.IN_PROGRESS));
    }
    
    @Override
    public List<IncidentReport> getUrgentIncidents() {
        return incidentRepository.findByIsUrgent(true);
    }
    
    @Override
    public List<IncidentReport> getOverdueIncidents() {
        return incidentRepository.findOverdueIncidents(LocalDateTime.now());
    }
    
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalIncidents", incidentRepository.count());
        stats.put("activeIncidents", incidentRepository.countByStatus(IncidentStatus.REPORTED) + 
                                    incidentRepository.countByStatus(IncidentStatus.UNDER_REVIEW) +
                                    incidentRepository.countByStatus(IncidentStatus.ASSIGNED) +
                                    incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS));
        stats.put("resolvedIncidents", incidentRepository.countByStatus(IncidentStatus.RESOLVED));
        stats.put("closedIncidents", incidentRepository.countByStatus(IncidentStatus.CLOSED));
        stats.put("overdueIncidents", getOverdueIncidents().size());
        stats.put("urgentIncidents", getUrgentIncidents().size());
        
        return stats;
    }
    
    @Override
    public Map<String, Long> getIncidentCountByStatus() {
        List<Object[]> results = incidentRepository.getIncidentCountByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((IncidentStatus) row[0]).getDisplayName(),
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    public Map<String, Long> getIncidentCountByCategory() {
        List<Object[]> results = incidentRepository.getIncidentCountByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    public Map<String, Long> getIncidentCountByPriority() {
        List<Object[]> results = incidentRepository.getIncidentCountByPriority();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> "Priority " + row[0],
                        row -> (Long) row[1]
                ));
    }
    
    @Override
    public List<IncidentReport> getRecentIncidents(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return incidentRepository.findRecentIncidents(pageable).getContent();
    }
    
    @Override
    public List<IncidentReport> getPendingIncidents() {
        return incidentRepository.findPendingIncidents();
    }
    
    @Override
    public List<IncidentReport> getActiveIncidents() {
        return incidentRepository.findActiveIncidents();
    }
    
    @Override
    public boolean canUserViewIncident(IncidentReport incident, User user) {
        // Admins can view all incidents
        if (user.getRole().isAdmin()) {
            return true;
        }
        
        // Maintenance staff can view incidents they're assigned to or all active incidents
        if (user.getRole().isMaintenance()) {
            return incident.getAssignedTo() != null && incident.getAssignedTo().equals(user) ||
                   !incident.getStatus().isResolved();
        }
        
        // Reporters can only view their own incidents
        return incident.getReporter() != null && incident.getReporter().equals(user);
    }
    
    @Override
    public boolean canUserUpdateIncident(IncidentReport incident, User user) {
        // Admins can update all incidents
        if (user.getRole().isAdmin()) {
            return true;
        }
        
        // Maintenance staff can update incidents they're assigned to OR incidents that are not yet assigned
        if (user.getRole().isMaintenance()) {
            return incident.getAssignedTo() == null || incident.getAssignedTo().equals(user);
        }
        
        // Reporters can only update their own incidents if they're still in REPORTED status
        return incident.getReporter() != null && incident.getReporter().equals(user) &&
               incident.getStatus() == IncidentStatus.REPORTED;
    }
    
    @Override
    public boolean canUserDeleteIncident(IncidentReport incident, User user) {
        // Only admins can delete incidents
        return user.getRole().isAdmin();
    }
    
    @Override
    public List<IncidentStatus> getAvailableStatusTransitions(IncidentReport incident, User user) {
        List<IncidentStatus> availableTransitions = new ArrayList<>();
        
        for (IncidentStatus status : IncidentStatus.values()) {
            if (incident.canTransitionTo(status)) {
                availableTransitions.add(status);
            }
        }
        
        return availableTransitions;
    }
    
    @Override
    public void sendStatusUpdateNotification(IncidentReport incident, IncidentStatus oldStatus, IncidentStatus newStatus) {
        // Implementation would integrate with notification service
        // For now, just log the notification
        System.out.println("Status update notification: Incident " + incident.getId() + 
                          " changed from " + oldStatus + " to " + newStatus);
    }
    
    @Override
    public void sendAssignmentNotification(IncidentReport incident, User assignee) {
        // Implementation would integrate with notification service
        System.out.println("Assignment notification: Incident " + incident.getId() + 
                          " assigned to " + assignee.getUsername());
    }
    
    @Override
    public void sendOverdueAlert(IncidentReport incident) {
        // Implementation would integrate with notification service
        System.out.println("Overdue alert: Incident " + incident.getId() + " is overdue");
    }
    
    @Override
    public List<IncidentReport> bulkUpdateStatus(List<Long> incidentIds, IncidentStatus newStatus, User updater, String notes) {
        List<IncidentReport> updatedIncidents = new ArrayList<>();
        
        for (Long incidentId : incidentIds) {
            try {
                IncidentReport updated = updateIncidentStatus(incidentId, newStatus, updater, notes);
                updatedIncidents.add(updated);
            } catch (Exception e) {
                // Log error and continue with other incidents
                System.err.println("Failed to update incident " + incidentId + ": " + e.getMessage());
            }
        }
        
        return updatedIncidents;
    }
    
    @Override
    public List<IncidentReport> bulkAssign(List<Long> incidentIds, Long assigneeId, User assigner) {
        List<IncidentReport> assignedIncidents = new ArrayList<>();
        
        for (Long incidentId : incidentIds) {
            try {
                IncidentReport assigned = assignIncident(incidentId, assigneeId, assigner);
                assignedIncidents.add(assigned);
            } catch (Exception e) {
                // Log error and continue with other incidents
                System.err.println("Failed to assign incident " + incidentId + ": " + e.getMessage());
            }
        }
        
        return assignedIncidents;
    }
    
    @Override
    public byte[] exportIncidentsToCSV(List<IncidentReport> incidents) {
        // Implementation would generate CSV export
        // For now, return empty array
        return new byte[0];
    }
    
    @Override
    public byte[] exportIncidentsToPDF(List<IncidentReport> incidents) {
        // Implementation would generate PDF export
        // For now, return empty array
        return new byte[0];
    }
    
    @Override
    public String generateIncidentReport(Long incidentId) {
        IncidentReport incident = getIncidentById(incidentId);
        
        StringBuilder report = new StringBuilder();
        report.append("INCIDENT REPORT\n");
        report.append("===============\n\n");
        report.append("ID: ").append(incident.getId()).append("\n");
        report.append("Title: ").append(incident.getTitle()).append("\n");
        report.append("Status: ").append(incident.getStatus().getDisplayName()).append("\n");
        report.append("Priority: ").append(incident.getPriorityLabel()).append("\n");
        report.append("Category: ").append(incident.getCategory().getName()).append("\n");
        report.append("Created: ").append(incident.getCreatedAt()).append("\n");
        report.append("Description: ").append(incident.getDescription()).append("\n");
        
        if (incident.getLocationDetails() != null) {
            report.append("Location: ").append(incident.getLocationDetails()).append("\n");
        }
        
        if (incident.getAssignedTo() != null) {
            report.append("Assigned to: ").append(incident.getAssignedTo().getUsername()).append("\n");
        }
        
        return report.toString();
    }
    
}
