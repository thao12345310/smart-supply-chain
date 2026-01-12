package com.distribution.security;

import com.distribution.dto.SalesOrderDTO;
import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.model.enums.PurchaseOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Aspect for filtering data based on user roles
 * 
 * Key Rules:
 * 1. Warehouse staff cannot see unapproved orders (only see APPROVED and later statuses)
 * 2. Shippers only see their assigned delivery trips
 */
@Aspect
@Component
@Slf4j
public class DataFilterAspect {

    /**
     * Filter sales orders for warehouse staff - they cannot see unapproved orders
     * Applied to service methods that return List<SalesOrderDTO>
     */
    @Around("execution(java.util.List<com.distribution.dto.SalesOrderDTO> com.distribution.service.*.get*(..))")
    public Object filterSalesOrdersForWarehouse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (result instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<SalesOrderDTO> orders = (List<SalesOrderDTO>) result;
            
            // Check if current user is warehouse staff (and not admin)
            if (SecurityUtils.hasRole("WAREHOUSE_STAFF") && !SecurityUtils.isAdmin()) {
                // Filter out unapproved orders
                List<SalesOrderDTO> filteredOrders = orders.stream()
                        .filter(order -> isApprovedOrLaterStatus(order.getStatus()))
                        .collect(Collectors.toList());
                
                log.debug("Filtered {} unapproved sales orders for warehouse staff", 
                        orders.size() - filteredOrders.size());
                
                return filteredOrders;
            }
        }
        
        return result;
    }

    /**
     * Filter purchase orders for warehouse staff - they cannot see unapproved orders
     * Applied to service methods that return List<PurchaseOrderDTO>
     */
    @Around("execution(java.util.List<com.distribution.dto.PurchaseOrderDTO> com.distribution.service.*.get*(..))")
    public Object filterPurchaseOrdersForWarehouse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (result instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PurchaseOrderDTO> orders = (List<PurchaseOrderDTO>) result;
            
            // Check if current user is warehouse staff (and not admin)
            if (SecurityUtils.hasRole("WAREHOUSE_STAFF") && !SecurityUtils.isAdmin()) {
                // Filter out unapproved orders (only show APPROVED and later)
                List<PurchaseOrderDTO> filteredOrders = orders.stream()
                        .filter(order -> isApprovedPurchaseOrderStatus(order.getStatus()))
                        .collect(Collectors.toList());
                
                log.debug("Filtered {} unapproved purchase orders for warehouse staff", 
                        orders.size() - filteredOrders.size());
                
                return filteredOrders;
            }
        }
        
        return result;
    }

    /**
     * Check if sales order status is APPROVED or later (can be seen by warehouse)
     */
    private boolean isApprovedOrLaterStatus(SalesOrderStatus status) {
        if (status == null) return false;
        return status == SalesOrderStatus.ORDER_APPROVED 
                || status == SalesOrderStatus.ORDER_PARTIALLY_DELIVERED
                || status == SalesOrderStatus.ORDER_COMPLETED;
    }

    /**
     * Check if purchase order status is APPROVED or later (can be seen by warehouse)
     */
    private boolean isApprovedPurchaseOrderStatus(PurchaseOrderStatus status) {
        if (status == null) return false;
        return status == PurchaseOrderStatus.ORDER_APPROVED 
                || status == PurchaseOrderStatus.ORDER_PARTIALLY_RECEIVED
                || status == PurchaseOrderStatus.ORDER_COMPLETED;
    }
}
