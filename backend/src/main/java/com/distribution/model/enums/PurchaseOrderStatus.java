package com.distribution.model.enums;

/**
 * Purchase Order Status Lifecycle:
 * ORDER_OPEN → ORDER_APPROVED → ORDER_COMPLETED / ORDER_CANCELLED
 * 
 * ORDER_OPEN: Initial state when PO is created by Purchasing Staff
 * ORDER_APPROVED: PO has been approved by Manager or Accountant
 * ORDER_PARTIALLY_RECEIVED: Some goods have been received (partial receiving)
 * ORDER_COMPLETED: All ordered goods have been received
 * ORDER_CANCELLED: PO has been cancelled/rejected
 */
public enum PurchaseOrderStatus {
    ORDER_OPEN("Order Open", "PO created, pending approval"),
    ORDER_APPROVED("Order Approved", "PO approved, ready for goods receipt"),
    ORDER_PARTIALLY_RECEIVED("Partially Received", "Some goods received, pending completion"),
    ORDER_COMPLETED("Order Completed", "All goods received, PO completed"),
    ORDER_CANCELLED("Order Cancelled", "PO has been cancelled or rejected");

    private final String displayName;
    private final String description;

    PurchaseOrderStatus(String displayName, String description) {
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
     * Check if PO can be approved from current status
     */
    public boolean canApprove() {
        return this == ORDER_OPEN;
    }

    /**
     * Check if PO can be cancelled from current status
     */
    public boolean canCancel() {
        return this == ORDER_OPEN || this == ORDER_APPROVED;
    }

    /**
     * Check if goods can be received for this PO
     */
    public boolean canReceiveGoods() {
        return this == ORDER_APPROVED || this == ORDER_PARTIALLY_RECEIVED;
    }
}
