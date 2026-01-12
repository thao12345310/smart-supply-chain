package com.distribution.model.enums;

/**
 * Payment Status for Sales Orders
 */
public enum PaymentStatus {
    UNPAID("Unpaid", "No payment received"),
    PARTIALLY_PAID("Partially Paid", "Some payment received"),
    PAID("Paid", "Fully paid"),
    REFUNDED("Refunded", "Payment refunded");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
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
