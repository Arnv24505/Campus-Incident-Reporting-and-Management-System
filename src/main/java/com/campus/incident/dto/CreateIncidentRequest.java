package com.campus.incident.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateIncidentRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String locationDetails;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private Integer priorityLevel;
    
    private Boolean isUrgent = false;
    
    private Boolean isConfidential = false;
    
    // Constructors
    public CreateIncidentRequest() {}
    
    public CreateIncidentRequest(String title, String description, Long categoryId) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean isUrgent) { this.isUrgent = isUrgent; }
    
    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }
}
