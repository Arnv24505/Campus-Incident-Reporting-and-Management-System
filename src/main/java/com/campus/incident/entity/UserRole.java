package com.campus.incident.entity;

public enum UserRole {
    REPORTER("Reporter", "Can submit incident reports"),
    MAINTENANCE("Maintenance Staff", "Can view and update assigned incidents"),
    ADMIN("Administrator", "Full access to all system features");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    public boolean isMaintenance() {
        return this == MAINTENANCE || this == ADMIN;
    }
    
    public boolean isReporter() {
        return this == REPORTER || this == MAINTENANCE || this == ADMIN;
    }
}
