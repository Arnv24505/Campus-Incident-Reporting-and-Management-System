package com.campus.incident.entity;

public enum     IncidentStatus {
    REPORTED("Reported", "Initial report submitted", 1),
    UNDER_REVIEW("Under Review", "Being reviewed by staff", 2),
    ASSIGNED("Assigned", "Assigned to maintenance staff", 3),
    IN_PROGRESS("In Progress", "Work has begun", 4),
    ON_HOLD("On Hold", "Work temporarily suspended", 5),
    RESOLVED("Resolved", "Issue has been fixed", 6),
    CLOSED("Closed", "Incident fully closed", 7),
    CANCELLED("Cancelled", "Report cancelled or invalid", 8);
    
    private final String displayName;
    private final String description;
    private final int order;
    
    IncidentStatus(String displayName, String description, int order) {
        this.displayName = displayName;
        this.description = description;
        this.order = order;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getOrder() {
        return order;
    }
    
    public boolean isActive() {
        return this != RESOLVED && this != CLOSED && this != CANCELLED;
    }
    
    public boolean isResolved() {
        return this == RESOLVED || this == CLOSED;
    }
    
    public boolean canTransitionTo(IncidentStatus newStatus) {
        switch (this) {
            case REPORTED:
                return newStatus == UNDER_REVIEW || newStatus == CANCELLED;
            case UNDER_REVIEW:
                return newStatus == ASSIGNED || newStatus == CANCELLED;
            case ASSIGNED:
                return newStatus == IN_PROGRESS || newStatus == ON_HOLD;
            case IN_PROGRESS:
                return newStatus == ON_HOLD || newStatus == RESOLVED;
            case ON_HOLD:
                return newStatus == IN_PROGRESS || newStatus == CANCELLED;
            case RESOLVED:
                return newStatus == CLOSED;
            case CLOSED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
    
    public static IncidentStatus getInitialStatus() {
        return REPORTED;
    }
    
    public static IncidentStatus getFinalStatus() {
        return CLOSED;
    }
}
