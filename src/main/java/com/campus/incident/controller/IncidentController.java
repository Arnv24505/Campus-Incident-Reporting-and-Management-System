package com.campus.incident.controller;

import com.campus.incident.dto.CreateIncidentRequest;
import com.campus.incident.entity.IncidentCategory;
import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.User;
import com.campus.incident.repository.IncidentCategoryRepository;
import com.campus.incident.repository.IncidentReportRepository;
import com.campus.incident.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
public class IncidentController {
    
    @Autowired
    private IncidentService incidentService;
    
    @Autowired
    private IncidentReportRepository incidentRepository;
    
    @Autowired
    private IncidentCategoryRepository categoryRepository;
    
    // Get all incidents (with pagination and filtering)
    @GetMapping
    public ResponseEntity<Page<IncidentReport>> getAllIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Integer priorityLevel,
            @RequestParam(required = false) Boolean isUrgent,
            @RequestParam(required = false) String search) {
        
        User currentUser = getCurrentUser();
        
        // Create sort object
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // For now, just return all incidents with basic pagination
        try {
            Page<IncidentReport> incidents = incidentRepository.findAll(pageable);
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    // Create new incident
    @PostMapping
    public ResponseEntity<IncidentReport> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        try {
            // Look up the category
            IncidentCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            
            // Create the incident entity
            IncidentReport incident = new IncidentReport();
            incident.setTitle(request.getTitle());
            incident.setDescription(request.getDescription());
            incident.setLocationDetails(request.getLocationDetails());
            incident.setCategory(category);
            incident.setPriorityLevel(request.getPriorityLevel());
            incident.setUrgent(request.getIsUrgent());
            incident.setConfidential(request.getIsConfidential());
            
            User currentUser = getCurrentUser();
            IncidentReport created = incidentService.createIncident(incident, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Simple incident creation endpoint (bypasses service layer for now)
    @PostMapping("/simple")
    public ResponseEntity<IncidentReport> createSimpleIncident(@RequestBody CreateIncidentRequest request) {
        try {
            // Look up the category
            IncidentCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            
            // Create the incident entity directly
            IncidentReport incident = new IncidentReport();
            incident.setTitle(request.getTitle());
            incident.setDescription(request.getDescription());
            incident.setLocationDetails(request.getLocationDetails());
            incident.setCategory(category);
            incident.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
            incident.setUrgent(request.getIsUrgent() != null ? request.getIsUrgent() : false);
            incident.setConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : false);
            incident.setStatus(IncidentStatus.REPORTED);
            incident.setCreatedAt(LocalDateTime.now());
            incident.setUpdatedAt(LocalDateTime.now());
            
            // Save directly to repository
            IncidentReport saved = incidentRepository.save(incident);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Test endpoint to echo back received data
    @PostMapping("/test")
    public ResponseEntity<String> testEndpoint(@RequestBody String rawData) {
        System.out.println("Raw data received: " + rawData);
        return ResponseEntity.ok("Received: " + rawData);
    }
    
    // Get incident by ID
    @GetMapping("/{id}")
    public ResponseEntity<IncidentReport> getIncident(@PathVariable Long id) {
        IncidentReport incident = incidentService.getIncidentById(id);
        User currentUser = getCurrentUser();
        
        if (!incidentService.canUserViewIncident(incident, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(incident);
    }
    
    // Update incident
    @PutMapping("/{id}")
    public ResponseEntity<IncidentReport> updateIncident(@PathVariable Long id, 
                                                       @Valid @RequestBody IncidentReport incidentDetails) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.updateIncident(id, incidentDetails, currentUser);
        return ResponseEntity.ok(updated);
    }
    
    // Delete incident
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        incidentService.deleteIncident(id, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    // Update incident status
    @PatchMapping("/{id}/status")
    public ResponseEntity<IncidentReport> updateStatus(@PathVariable Long id,
                                                     @RequestParam IncidentStatus status,
                                                     @RequestParam(required = false) String notes) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.updateIncidentStatus(id, status, currentUser, notes);
        return ResponseEntity.ok(updated);
    }
    
    // Assign incident
    @PatchMapping("/{id}/assign")
    public ResponseEntity<IncidentReport> assignIncident(@PathVariable Long id,
                                                       @RequestParam Long assigneeId) {
        User currentUser = getCurrentUser();
        IncidentReport assigned = incidentService.assignIncident(id, assigneeId, currentUser);
        return ResponseEntity.ok(assigned);
    }
    
    // Start work on incident
    @PatchMapping("/{id}/start-work")
    public ResponseEntity<IncidentReport> startWork(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.startWork(id, currentUser);
        return ResponseEntity.ok(updated);
    }
    
    // Pause work on incident
    @PatchMapping("/{id}/pause-work")
    public ResponseEntity<IncidentReport> pauseWork(@PathVariable Long id,
                                                  @RequestParam String reason) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.pauseWork(id, currentUser, reason);
        return ResponseEntity.ok(updated);
    }
    
    // Complete work on incident
    @PatchMapping("/{id}/complete-work")
    public ResponseEntity<IncidentReport> completeWork(@PathVariable Long id,
                                                     @RequestParam String resolutionNotes) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.completeWork(id, currentUser, resolutionNotes);
        return ResponseEntity.ok(updated);
    }
    
    // Close incident
    @PatchMapping("/{id}/close")
    public ResponseEntity<IncidentReport> closeIncident(@PathVariable Long id,
                                                      @RequestParam String closureNotes) {
        User currentUser = getCurrentUser();
        IncidentReport updated = incidentService.closeIncident(id, currentUser, closureNotes);
        return ResponseEntity.ok(updated);
    }
    
    // Add resolution log
    @PostMapping("/{id}/logs")
    public ResponseEntity<Void> addResolutionLog(@PathVariable Long id,
                                               @RequestParam String action,
                                               @RequestParam String notes) {
        User currentUser = getCurrentUser();
        incidentService.addResolutionLog(id, action, notes, currentUser);
        return ResponseEntity.ok().build();
    }
    
    // Add time log
    @PostMapping("/{id}/time-logs")
    public ResponseEntity<Void> addTimeLog(@PathVariable Long id,
                                         @RequestParam Integer minutesSpent,
                                         @RequestParam String notes) {
        User currentUser = getCurrentUser();
        incidentService.addTimeLog(id, minutesSpent, notes, currentUser);
        return ResponseEntity.ok().build();
    }
    
    // Add cost log
    @PostMapping("/{id}/cost-logs")
    public ResponseEntity<Void> addCostLog(@PathVariable Long id,
                                         @RequestParam Double cost,
                                         @RequestParam String description) {
        User currentUser = getCurrentUser();
        incidentService.addCostLog(id, cost, description, currentUser);
        return ResponseEntity.ok().build();
    }
    
    // Add material log
    @PostMapping("/{id}/material-logs")
    public ResponseEntity<Void> addMaterialLog(@PathVariable Long id,
                                             @RequestParam String materials,
                                             @RequestParam String notes) {
        User currentUser = getCurrentUser();
        incidentService.addMaterialLog(id, materials, notes, currentUser);
        return ResponseEntity.ok().build();
    }
    
    // Search incidents
    @GetMapping("/search")
    public ResponseEntity<Page<IncidentReport>> searchIncidents(
            @RequestParam(required = false, defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<IncidentReport> incidents = incidentService.searchIncidents(searchTerm, pageable);
        return ResponseEntity.ok(incidents);
    }
    
    // Get incidents by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<IncidentReport>> getIncidentsByStatus(@PathVariable IncidentStatus status) {
        List<IncidentReport> incidents = incidentService.getIncidentsByStatus(status);
        return ResponseEntity.ok(incidents);
    }
    
    // Get incidents by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<IncidentReport>> getIncidentsByCategory(@PathVariable Long categoryId) {
        List<IncidentReport> incidents = incidentService.getIncidentsByCategory(categoryId);
        return ResponseEntity.ok(incidents);
    }
    
    // Get incidents by reporter
    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<List<IncidentReport>> getIncidentsByReporter(@PathVariable Long reporterId) {
        List<IncidentReport> incidents = incidentService.getIncidentsByReporter(reporterId);
        return ResponseEntity.ok(incidents);
    }
    
    // Get incidents by assignee
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<IncidentReport>> getIncidentsByAssignee(@PathVariable Long assigneeId) {
        List<IncidentReport> incidents = incidentService.getIncidentsByAssignee(assigneeId);
        return ResponseEntity.ok(incidents);
    }
    
    // Get high priority incidents
    @GetMapping("/priority/{minPriority}")
    public ResponseEntity<List<IncidentReport>> getHighPriorityIncidents(@PathVariable Integer minPriority) {
        List<IncidentReport> incidents = incidentService.getIncidentsByPriority(minPriority);
        return ResponseEntity.ok(incidents);
    }
    
    // Get urgent incidents
    @GetMapping("/urgent")
    public ResponseEntity<List<IncidentReport>> getUrgentIncidents() {
        List<IncidentReport> incidents = incidentService.getUrgentIncidents();
        return ResponseEntity.ok(incidents);
    }
    
    // Get overdue incidents
    @GetMapping("/overdue")
    public ResponseEntity<List<IncidentReport>> getOverdueIncidents() {
        List<IncidentReport> incidents = incidentService.getOverdueIncidents();
        return ResponseEntity.ok(incidents);
    }
    
    // Get dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> stats = incidentService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }
    
    // Get incident count by status
    @GetMapping("/dashboard/status-count")
    public ResponseEntity<Map<String, Long>> getIncidentCountByStatus() {
        Map<String, Long> counts = incidentService.getIncidentCountByStatus();
        return ResponseEntity.ok(counts);
    }
    
    // Get incident count by category
    @GetMapping("/dashboard/category-count")
    public ResponseEntity<Map<String, Long>> getIncidentCountByCategory() {
        Map<String, Long> counts = incidentService.getIncidentCountByCategory();
        return ResponseEntity.ok(counts);
    }
    
    // Get incident count by priority
    @GetMapping("/dashboard/priority-count")
    public ResponseEntity<Map<String, Long>> getIncidentCountByPriority() {
        Map<String, Long> counts = incidentService.getIncidentCountByPriority();
        return ResponseEntity.ok(counts);
    }
    
    // Get recent incidents
    @GetMapping("/recent")
    public ResponseEntity<List<IncidentReport>> getRecentIncidents(
            @RequestParam(defaultValue = "10") int limit) {
        List<IncidentReport> incidents = incidentService.getRecentIncidents(limit);
        return ResponseEntity.ok(incidents);
    }
    
    // Get pending incidents
    @GetMapping("/pending")
    public ResponseEntity<List<IncidentReport>> getPendingIncidents() {
        List<IncidentReport> incidents = incidentService.getPendingIncidents();
        return ResponseEntity.ok(incidents);
    }
    
    // Get active incidents
    @GetMapping("/active")
    public ResponseEntity<List<IncidentReport>> getActiveIncidents() {
        List<IncidentReport> incidents = incidentService.getActiveIncidents();
        return ResponseEntity.ok(incidents);
    }
    
    // Get available status transitions
    @GetMapping("/{id}/available-statuses")
    public ResponseEntity<List<IncidentStatus>> getAvailableStatusTransitions(@PathVariable Long id) {
        IncidentReport incident = incidentService.getIncidentById(id);
        User currentUser = getCurrentUser();
        List<IncidentStatus> transitions = incidentService.getAvailableStatusTransitions(incident, currentUser);
        return ResponseEntity.ok(transitions);
    }
    
    // Bulk operations
    @PatchMapping("/bulk/status")
    public ResponseEntity<List<IncidentReport>> bulkUpdateStatus(
            @RequestParam List<Long> incidentIds,
            @RequestParam IncidentStatus status,
            @RequestParam(required = false) String notes) {
        User currentUser = getCurrentUser();
        List<IncidentReport> updated = incidentService.bulkUpdateStatus(incidentIds, status, currentUser, notes);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/bulk/assign")
    public ResponseEntity<List<IncidentReport>> bulkAssign(
            @RequestParam List<Long> incidentIds,
            @RequestParam Long assigneeId) {
        User currentUser = getCurrentUser();
        List<IncidentReport> assigned = incidentService.bulkAssign(incidentIds, assigneeId, currentUser);
        return ResponseEntity.ok(assigned);
    }
    
    // Export incidents
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCSV(@RequestParam List<Long> incidentIds) {
        List<IncidentReport> incidents = incidentIds.stream()
                .map(incidentService::getIncidentById)
                .toList();
        
        byte[] csvData = incidentService.exportIncidentsToCSV(incidents);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=incidents.csv")
                .body(csvData);
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPDF(@RequestParam List<Long> incidentIds) {
        List<IncidentReport> incidents = incidentIds.stream()
                .map(incidentService::getIncidentById)
                .toList();
        
        byte[] pdfData = incidentService.exportIncidentsToPDF(incidents);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=incidents.pdf")
                .body(pdfData);
    }
    
    // Generate incident report
    @GetMapping("/{id}/report")
    public ResponseEntity<String> generateIncidentReport(@PathVariable Long id) {
        String report = incidentService.generateIncidentReport(id);
        return ResponseEntity.ok(report);
    }
    
    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // This is a simplified version - in a real application, you'd get the actual user from the database
        // For now, we'll create a mock user for demonstration
        User user = new User();
        user.setUsername(authentication.getName());
        user.setRole(com.campus.incident.entity.UserRole.ADMIN); // Default to admin for demo
        return user;
    }
}
