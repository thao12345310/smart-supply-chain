package com.distribution.service;

import com.distribution.dto.PurchaseOrderDTO;
import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.model.enums.PurchaseOrderStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Purchase Order operations
 */
public interface PurchaseOrderService {
    
    /**
     * Create a new Purchase Order
     * Sets initial status to ORDER_OPEN
     */
    PurchaseOrderDTO create(PurchaseOrderDTO dto);
    
    /**
     * Update an existing Purchase Order
     * Only allowed when status is ORDER_OPEN
     */
    PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto);
    
    /**
     * Get Purchase Order by ID
     */
    PurchaseOrderDTO getById(Long id);
    
    /**
     * Get Purchase Order by code
     */
    PurchaseOrderDTO getByCode(String code);
    
    /**
     * Get all Purchase Orders
     */
    List<PurchaseOrderDTO> getAll();
    
    /**
     * Get Purchase Orders by status
     */
    List<PurchaseOrderDTO> getByStatus(PurchaseOrderStatus status);
    
    /**
     * Get Purchase Orders ready for goods receipt
     */
    List<PurchaseOrderDTO> getReadyForGoodsReceipt();
    
    /**
     * Get Purchase Orders pending approval
     */
    List<PurchaseOrderDTO> getPendingApproval();
    
    /**
     * Get Purchase Orders by supplier
     */
    List<PurchaseOrderDTO> getBySupplierId(Long supplierId);
    
    /**
     * Get Purchase Orders by date range
     */
    List<PurchaseOrderDTO> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Delete Purchase Order (soft delete or only when OPEN)
     */
    void delete(Long id);
    
    /**
     * Approve or Reject a Purchase Order
     * Only Manager or Accountant can perform this action
     */
    PurchaseOrderDTO processApproval(Long id, ApprovalRequestDTO approvalRequest);
    
    /**
     * Approve a Purchase Order
     */
    PurchaseOrderDTO approve(Long id, Long approvedBy);
    
    /**
     * Reject a Purchase Order
     */
    PurchaseOrderDTO reject(Long id, Long rejectedBy, String reason);
    
    /**
     * Cancel a Purchase Order
     */
    PurchaseOrderDTO cancel(Long id, String reason);
    
    /**
     * Save/Update Purchase Order (for internal use)
     */
    PurchaseOrderDTO save(PurchaseOrderDTO dto);
    
    /**
     * Update PO status based on receiving progress
     * Called after goods receipt confirmation
     */
    void updateReceivingStatus(Long poId);
}
