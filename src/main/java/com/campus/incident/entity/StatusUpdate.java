package com.campus.incident.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_updates")
@EntityListeners(AuditingEntityListener.class)
public class StatusUpdate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @NotNull(message = "Incident is required")
    private IncidentReport incident;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false)
    @NotNull(message = "Previous status is required")
    private IncidentStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    @NotNull(message = "New status is required")
    private IncidentStatus newStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id", nullable = false)
    @NotNull(message = "Updated by user is required")
    private User updatedBy;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "updated_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime updatedAt;
    
    @Column(name = "transition_reason")
    @Size(max = 200, message = "Transition reason cannot exceed 200 characters")
    private String transitionReason;
    
    @Column(name = "estimated_completion_date")
    private LocalDateTime estimatedCompletionDate;
    
    // Constructors
    public StatusUpdate() {}
    
    public StatusUpdate(IncidentStatus previousStatus, IncidentStatus newStatus, User updatedBy, String notes) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public IncidentReport getIncident() { return incident; }
    public void setIncident(IncidentReport incident) { this.incident = incident; }
    
    public IncidentStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(IncidentStatus previousStatus) { this.previousStatus = previousStatus; }
    
    public IncidentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(IncidentStatus newStatus) { this.newStatus = newStatus; }
    
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTransitionReason() { return transitionReason; }
    public void setTransitionReason(String transitionReason) { this.transitionReason = transitionReason; }
    
    public LocalDateTime getEstimatedCompletionDate() { return estimatedCompletionDate; }
    public void setEstimatedCompletionDate(LocalDateTime estimatedCompletionDate) { this.estimatedCompletionDate = estimatedCompletionDate; }
    
    // Business Methods
    public boolean isStatusUpgrade() {
        return newStatus.getOrder() > previousStatus.getOrder();
    }
    
    public boolean isStatusDowngrade() {
        return newStatus.getOrder() < previousStatus.getOrder();
    }
    
    public boolean isResolutionTransition() {
        return newStatus.isResolved() && !previousStatus.isResolved();
    }
    
    public boolean isAssignmentTransition() {
        return newStatus == IncidentStatus.ASSIGNED && previousStatus == IncidentStatus.UNDER_REVIEW;
    }
    
    public String getStatusChangeDescription() {
        if (isStatusUpgrade()) {
            return "Status upgraded from " + previousStatus.getDisplayName() + " to " + newStatus.getDisplayName();
        } else if (isStatusDowngrade()) {
            return "Status downgraded from " + previousStatus.getDisplayName() + " to " + newStatus.getDisplayName();
        } else {
            return "Status changed from " + previousStatus.getDisplayName() + " to " + newStatus.getDisplayName();
        }
    }
    
    public boolean requiresNotification() {
        return isStatusUpgrade() || isResolutionTransition() || isAssignmentTransition();
    }
    
    @Override
    public String toString() {
        return "StatusUpdate{" +
                "id=" + id +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", updatedBy=" + (updatedBy != null ? updatedBy.getUsername() : "Unknown") +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
