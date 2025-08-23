package com.campus.incident.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "resolution_logs")
@EntityListeners(AuditingEntityListener.class)
public class ResolutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @NotNull(message = "Incident is required")
    private IncidentReport incident;
    
    @NotBlank(message = "Action is required")
    @Size(min = 3, max = 200, message = "Action must be between 3 and 200 characters")
    @Column(nullable = false)
    private String action;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id", nullable = false)
    @NotNull(message = "Performer is required")
    private User performedBy;
    
    @Column(name = "performed_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime performedAt;
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;
    
    @Column(name = "materials_used")
    @Size(max = 500, message = "Materials used cannot exceed 500 characters")
    private String materialsUsed;
    
    @Column(name = "cost_incurred")
    private Double costIncurred;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type")
    private LogType logType = LogType.WORK_LOG;
    
    // Constructors
    public ResolutionLog() {}
    
    public ResolutionLog(String action, String notes, User performedBy) {
        this.action = action;
        this.notes = notes;
        this.performedBy = performedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public IncidentReport getIncident() { return incident; }
    public void setIncident(IncidentReport incident) { this.incident = incident; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }
    
    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
    
    public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
    
    public String getMaterialsUsed() { return materialsUsed; }
    public void setMaterialsUsed(String materialsUsed) { this.materialsUsed = materialsUsed; }
    
    public Double getCostIncurred() { return costIncurred; }
    public void setCostIncurred(Double costIncurred) { this.costIncurred = costIncurred; }
    
    public LogType getLogType() { return logType; }
    public void setLogType(LogType logType) { this.logType = logType; }
    
    // Business Methods
    public String getFormattedTimeSpent() {
        if (timeSpentMinutes == null) return "N/A";
        
        int hours = timeSpentMinutes / 60;
        int minutes = timeSpentMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    public String getFormattedCost() {
        if (costIncurred == null) return "N/A";
        return String.format("$%.2f", costIncurred);
    }
    
    public boolean isWorkLog() {
        return logType == LogType.WORK_LOG;
    }
    
    public boolean isNote() {
        return logType == LogType.NOTE;
    }
    
    public boolean isCostLog() {
        return logType == LogType.COST_LOG;
    }
    
    @Override
    public String toString() {
        return "ResolutionLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", performedBy=" + (performedBy != null ? performedBy.getUsername() : "Unknown") +
                ", performedAt=" + performedAt +
                ", logType=" + logType +
                '}';
    }
    
    public enum LogType {
        WORK_LOG("Work Log", "Actual work performed"),
        NOTE("Note", "General note or comment"),
        COST_LOG("Cost Log", "Cost tracking entry"),
        MATERIAL_LOG("Material Log", "Materials used"),
        TIME_LOG("Time Log", "Time tracking entry");
        
        private final String displayName;
        private final String description;
        
        LogType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
