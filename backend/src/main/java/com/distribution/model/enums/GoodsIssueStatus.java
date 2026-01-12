package com.distribution.model.enums;

/**
 * Goods Issue (Outbound) Status Lifecycle:
 * DRAFT → CONFIRMED / CANCELLED
 * 
 * DRAFT: Initial state when Goods Issue is created
 * CONFIRMED: Goods Issue confirmed, inventory is decreased
 * CANCELLED: Goods Issue cancelled
 */
public enum GoodsIssueStatus {
    DRAFT("Draft", "Goods issue created, pending confirmation"),
    CONFIRMED("Confirmed", "Goods issued and inventory decreased"),
    CANCELLED("Cancelled", "Goods issue cancelled");

    private final String displayName;
    private final String description;

    GoodsIssueStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if goods issue can be confirmed
     */
    public boolean canConfirm() {
        return this == DRAFT;
    }

    /**
     * Check if goods issue can be cancelled
     */
    public boolean canCancel() {
        return this == DRAFT;
    }
}
