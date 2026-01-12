package com.distribution.model.enums;

/**
 * Sales Order Status Lifecycle:
 * ORDER_OPEN → ORDER_APPROVED → ORDER_COMPLETED / ORDER_CANCELLED
 * 
 * ORDER_OPEN: Initial state when SO is created by Sales Staff
 * ORDER_APPROVED: SO has been approved by Manager or Accountant
 * ORDER_PARTIALLY_DELIVERED: Some goods have been issued (partial delivery)
 * ORDER_COMPLETED: All ordered goods have been delivered
 * ORDER_CANCELLED: SO has been cancelled/rejected
 */
public enum SalesOrderStatus {
    ORDER_OPEN("Order Open", "SO created, pending approval"),
    ORDER_APPROVED("Order Approved", "SO approved, ready for goods issue"),
    ORDER_PARTIALLY_DELIVERED("Partially Delivered", "Some goods delivered, pending completion"),
    ORDER_COMPLETED("Order Completed", "All goods delivered, SO completed"),
    ORDER_CANCELLED("Order Cancelled", "SO has been cancelled or rejected");

    private final String displayName;
    private final String description;

    SalesOrderStatus(String displayName, String description) {
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
     * Check if SO can be approved from current status
     */
    public boolean canApprove() {
        return this == ORDER_OPEN;
    }

    /**
     * Check if SO can be cancelled from current status
     */
    public boolean canCancel() {
        return this == ORDER_OPEN || this == ORDER_APPROVED;
    }

    /**
     * Check if goods can be issued for this SO
     */
    public boolean canIssueGoods() {
        return this == ORDER_APPROVED || this == ORDER_PARTIALLY_DELIVERED;
    }
}
