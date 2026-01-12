package com.distribution.model.enums;

/**
 * Role types for role-based access control
 */
public enum RoleType {
    ROLE_ADMIN("System Administrator", true, true, true, true, true),
    ROLE_PURCHASING_STAFF("Purchasing Staff", true, false, false, false, false),
    ROLE_PURCHASING_MANAGER("Purchasing Manager", true, true, false, false, false),
    ROLE_ACCOUNTANT("Accountant", false, true, false, true, false),
    ROLE_WAREHOUSE_STAFF("Warehouse Staff", false, false, true, false, false),
    ROLE_SUPPLIER("Supplier", false, false, false, false, false);

    private final String displayName;
    private final boolean canCreatePO;
    private final boolean canApprovePO;
    private final boolean canReceiveGoods;
    private final boolean canViewAccounting;
    private final boolean canManageAll;

    RoleType(String displayName, boolean canCreatePO, boolean canApprovePO, 
             boolean canReceiveGoods, boolean canViewAccounting, boolean canManageAll) {
        this.displayName = displayName;
        this.canCreatePO = canCreatePO;
        this.canApprovePO = canApprovePO;
        this.canReceiveGoods = canReceiveGoods;
        this.canViewAccounting = canViewAccounting;
        this.canManageAll = canManageAll;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canCreatePO() {
        return canCreatePO || canManageAll;
    }

    public boolean canApprovePO() {
        return canApprovePO || canManageAll;
    }

    public boolean canReceiveGoods() {
        return canReceiveGoods || canManageAll;
    }

    public boolean canViewAccounting() {
        return canViewAccounting || canManageAll;
    }
}
