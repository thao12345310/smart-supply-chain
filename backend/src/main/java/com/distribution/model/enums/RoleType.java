package com.distribution.model.enums;

/**
 * Role types for Role-Based Access Control (RBAC)
 * 
 * Role Hierarchy and Permissions:
 * - ADMIN: Full system access
 * - PURCHASE_MANAGER: Manage purchases + approve POs
 * - PURCHASE_STAFF: Create and manage purchase orders
 * - SALES_MANAGER: Manage sales + approve SOs
 * - SALES_STAFF: Create and manage sales orders
 * - WAREHOUSE_STAFF: Manage goods receipt/issue and inventory
 * - DELIVERY_ADMIN: Manage delivery plans and assign shippers
 * - SHIPPER: Handle assigned deliveries only
 * - ACCOUNTANT: Approve orders (PO/SO) and view financial data
 */
public enum RoleType {
    ADMIN("System Administrator", "Full system access"),
    PURCHASE_STAFF("Purchasing Staff", "Create and manage purchase orders"),
    PURCHASE_MANAGER("Purchasing Manager", "Manage purchases and approve purchase orders"),
    SALES_STAFF("Sales Staff", "Create and manage sales orders"),
    SALES_MANAGER("Sales Manager", "Manage sales and approve sales orders"),
    WAREHOUSE_STAFF("Warehouse Staff", "Manage goods receipt/issue and inventory"),
    DELIVERY_ADMIN("Delivery Administrator", "Manage delivery plans and assign shippers"),
    SHIPPER("Shipper", "Handle assigned delivery trips only"),
    ACCOUNTANT("Accountant", "Approve orders and view financial data");

    private final String displayName;
    private final String description;

    RoleType(String displayName, String description) {
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
     * Get the Spring Security role name (with ROLE_ prefix)
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }

    /**
     * Check if this role can approve purchase orders
     */
    public boolean canApprovePurchaseOrder() {
        return this == ADMIN || this == PURCHASE_MANAGER || this == ACCOUNTANT;
    }

    /**
     * Check if this role can approve sales orders
     */
    public boolean canApproveSalesOrder() {
        return this == ADMIN || this == SALES_MANAGER || this == ACCOUNTANT;
    }

    /**
     * Check if this role can create purchase orders
     */
    public boolean canCreatePurchaseOrder() {
        return this == ADMIN || this == PURCHASE_MANAGER || this == PURCHASE_STAFF;
    }

    /**
     * Check if this role can create sales orders
     */
    public boolean canCreateSalesOrder() {
        return this == ADMIN || this == SALES_MANAGER || this == SALES_STAFF;
    }

    /**
     * Check if this role can manage warehouse operations
     */
    public boolean canManageWarehouse() {
        return this == ADMIN || this == WAREHOUSE_STAFF;
    }

    /**
     * Check if this role can manage delivery plans
     */
    public boolean canManageDelivery() {
        return this == ADMIN || this == DELIVERY_ADMIN;
    }

    /**
     * Check if this role can view only assigned trips (restricted view)
     */
    public boolean hasRestrictedTripView() {
        return this == SHIPPER;
    }

    /**
     * Check if this role can see unapproved orders
     */
    public boolean canSeeUnapprovedOrders() {
        return this != WAREHOUSE_STAFF;
    }

    /**
     * Check if this role has manager-level permissions
     */
    public boolean isManager() {
        return this == ADMIN || this == PURCHASE_MANAGER || this == SALES_MANAGER;
    }

    /**
     * Check if this role has approval permissions
     */
    public boolean canApprove() {
        return this == ADMIN || this == PURCHASE_MANAGER || this == SALES_MANAGER || this == ACCOUNTANT;
    }
}
