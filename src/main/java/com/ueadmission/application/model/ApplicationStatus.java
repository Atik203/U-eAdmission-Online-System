package com.ueadmission.application.model;

/**
 * Enum representing the various statuses an application can have
 */
public enum ApplicationStatus {
    PENDING("Pending", "status-pending"),
    UNDER_REVIEW("Under Review", "status-under-review"),
    APPROVED("Approved", "status-approved"),
    REJECTED("Rejected", "status-rejected"),
    INCOMPLETE("Incomplete", "status-incomplete");
    
    private final String displayName;
    private final String styleClass;
    
    ApplicationStatus(String displayName, String styleClass) {
        this.displayName = displayName;
        this.styleClass = styleClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getStyleClass() {
        return styleClass;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}