package com.distribution.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom security annotations for method-level access control
 */
public class SecurityAnnotations {

    /**
     * Requires user to have approval permissions (manager, accountant, or admin)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'SALES_MANAGER', 'ACCOUNTANT')")
    public @interface RequireApprovalPermission {
    }

    /**
     * Requires user to be a manager (purchase, sales, or admin)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'SALES_MANAGER')")
    public @interface RequireManager {
    }

    /**
     * Requires user to have purchase module access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_MANAGER', 'PURCHASE_STAFF')")
    public @interface RequirePurchaseAccess {
    }

    /**
     * Requires user to have sales module access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER', 'SALES_STAFF')")
    public @interface RequireSalesAccess {
    }

    /**
     * Requires user to have warehouse access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public @interface RequireWarehouseAccess {
    }

    /**
     * Requires user to have delivery management access
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'DELIVERY_ADMIN')")
    public @interface RequireDeliveryAccess {
    }

    /**
     * Requires admin role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface RequireAdmin {
    }

    /**
     * Requires accountant or admin role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public @interface RequireAccountant {
    }
}
