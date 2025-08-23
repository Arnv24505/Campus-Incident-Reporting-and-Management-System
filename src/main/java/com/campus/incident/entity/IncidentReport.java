package com.campus.incident.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident_reports")
@EntityListeners(AuditingEntityListener.class)
public class IncidentReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "location_details")
    @Size(max = 500, message = "Location details cannot exceed 500 characters")
    private String locationDetails;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private IncidentCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.REPORTED;
    
    @Column(name = "priority_level")
    private Integer priorityLevel = 1;
    
    @Column(name = "estimated_resolution_date")
    private LocalDateTime estimatedResolutionDate;
    
    @Column(name = "actual_resolution_date")
    private LocalDateTime actualResolutionDate;
    
    @Column(name = "is_urgent")
    private boolean isUrgent = false;
    
    @Column(name = "is_confidential")
    private boolean isConfidential = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("performedAt DESC")
    private List<ResolutionLog> resolutionLogs = new ArrayList<>();
    
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("updatedAt DESC")
    private List<StatusUpdate> statusUpdates = new ArrayList<>();
    
    // Constructors
    public IncidentReport() {}
    
    public IncidentReport(String title, String description, IncidentCategory category) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = IncidentStatus.REPORTED;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }
    
    public IncidentCategory getCategory() { return category; }
    public void setCategory(IncidentCategory category) { this.category = category; }
    
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public LocalDateTime getEstimatedResolutionDate() { return estimatedResolutionDate; }
    public void setEstimatedResolutionDate(LocalDateTime estimatedResolutionDate) { this.estimatedResolutionDate = estimatedResolutionDate; }
    
    public LocalDateTime getActualResolutionDate() { return actualResolutionDate; }
    public void setActualResolutionDate(LocalDateTime actualResolutionDate) { this.actualResolutionDate = actualResolutionDate; }
    
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    
    public boolean isConfidential() { return isConfidential; }
    public void setConfidential(boolean confidential) { isConfidential = confidential; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ResolutionLog> getResolutionLogs() { return resolutionLogs; }
    public void setResolutionLogs(List<ResolutionLog> resolutionLogs) { this.resolutionLogs = resolutionLogs; }
    
    public List<StatusUpdate> getStatusUpdates() { return statusUpdates; }
    public void setStatusUpdates(List<StatusUpdate> statusUpdates) { this.statusUpdates = statusUpdates; }
    
    // Business Methods
    public boolean canTransitionTo(IncidentStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }
    
    public void updateStatus(IncidentStatus newStatus, User updatedBy, String notes) {
        if (canTransitionTo(newStatus)) {
            this.status = newStatus;
            
            // Add status update log
            StatusUpdate statusUpdate = new StatusUpdate();
            statusUpdate.setIncident(this);
            statusUpdate.setPreviousStatus(this.status);
            statusUpdate.setNewStatus(newStatus);
            statusUpdate.setUpdatedBy(updatedBy);
            statusUpdate.setNotes(notes);
            statusUpdate.setUpdatedAt(LocalDateTime.now());
            
            this.statusUpdates.add(statusUpdate);
            
            // Set resolution date if resolved
            if (newStatus.isResolved()) {
                this.actualResolutionDate = LocalDateTime.now();
            }
        }
    }
    
    public void assignTo(User user) {
        this.assignedTo = user;
        if (this.status == IncidentStatus.UNDER_REVIEW) {
            updateStatus(IncidentStatus.ASSIGNED, user, "Incident assigned to " + user.getUsername());
        }
    }
    
    public void addResolutionLog(String action, String notes, User performedBy) {
        ResolutionLog log = new ResolutionLog();
        log.setIncident(this);
        log.setAction(action);
        log.setNotes(notes);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(LocalDateTime.now());
        
        this.resolutionLogs.add(log);
    }
    
    public boolean isOverdue() {
        if (estimatedResolutionDate == null || status.isResolved()) {
            return false;
        }
        return LocalDateTime.now().isAfter(estimatedResolutionDate);
    }
    
    public long getDaysSinceCreation() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }
    
    public String getPriorityLabel() {
        switch (priorityLevel) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            case 4: return "Critical";
            default: return "Unknown";
        }
    }
    
    public boolean isHighPriority() {
        return priorityLevel >= 3 || isUrgent;
    }
    
    @Override
    public String toString() {
        return "IncidentReport{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priorityLevel=" + priorityLevel +
                ", isUrgent=" + isUrgent +
                '}';
    }
}
