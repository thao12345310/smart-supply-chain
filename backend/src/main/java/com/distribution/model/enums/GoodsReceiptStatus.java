package com.distribution.model.enums;

/**
 * Goods Receipt Status Lifecycle:
 * DRAFT → CONFIRMED → CANCELLED
 * 
 * DRAFT: GR created but not yet confirmed
 * CONFIRMED: GR confirmed, inventory updated
 * CANCELLED: GR cancelled
 */
public enum GoodsReceiptStatus {
    DRAFT("Draft", "Goods receipt created, pending confirmation"),
    CONFIRMED("Confirmed", "Goods receipt confirmed, inventory updated"),
    CANCELLED("Cancelled", "Goods receipt cancelled");

    private final String displayName;
    private final String description;

    GoodsReceiptStatus(String displayName, String description) {
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
     * Check if GR can be confirmed from current status
     */
    public boolean canConfirm() {
        return this == DRAFT;
    }

    /**
     * Check if GR can be cancelled from current status
     */
    public boolean canCancel() {
        return this == DRAFT;
    }
}
