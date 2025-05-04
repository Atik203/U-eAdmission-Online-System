package com.ueadmission.application.model;

/**
 * Enum representing the various payment statuses an application can have
 */
public enum PaymentStatus {
    PAID("Paid", "payment-paid"),
    UNPAID("Unpaid", "payment-unpaid"),
    PARTIAL("Partial", "payment-partial");
    
    private final String displayName;
    private final String styleClass;
    
    PaymentStatus(String displayName, String styleClass) {
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