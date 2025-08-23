package com.campus.incident.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "incident_categories")
@EntityListeners(AuditingEntityListener.class)
public class IncidentCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(unique = true, nullable = false)
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1 = Low, 2 = Medium, 3 = High, 4 = Critical
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "estimated_resolution_time_hours")
    private Integer estimatedResolutionTimeHours = 24;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<IncidentReport> incidents = new HashSet<>();
    
    // Constructors
    public IncidentCategory() {}
    
    public IncidentCategory(String name, String description, Integer priorityLevel) {
        this.name = name;
        this.description = description;
        this.priorityLevel = priorityLevel;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Integer getEstimatedResolutionTimeHours() { return estimatedResolutionTimeHours; }
    public void setEstimatedResolutionTimeHours(Integer estimatedResolutionTimeHours) { this.estimatedResolutionTimeHours = estimatedResolutionTimeHours; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<IncidentReport> getIncidents() { return incidents; }
    public void setIncidents(Set<IncidentReport> incidents) { this.incidents = incidents; }
    
    // Business Methods
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
        return priorityLevel >= 3;
    }
    
    public boolean isCritical() {
        return priorityLevel == 4;
    }
    
    @Override
    public String toString() {
        return "IncidentCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priorityLevel=" + priorityLevel +
                ", isActive=" + isActive +
                '}';
    }
}
