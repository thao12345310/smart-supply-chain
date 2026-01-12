package com.distribution.service;

import com.distribution.dto.SalesOrderDTO;
import com.distribution.dto.ApprovalRequestDTO;
import com.distribution.model.enums.SalesOrderStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Sales Order operations
 */
public interface SalesOrderService {
    
    /**
     * Create a new Sales Order
     * Sets initial status to ORDER_OPEN
     */
    SalesOrderDTO create(SalesOrderDTO dto);
    
    /**
     * Update an existing Sales Order
     * Only allowed when status is ORDER_OPEN
     */
    SalesOrderDTO update(Long id, SalesOrderDTO dto);
    
    /**
     * Get Sales Order by ID
     */
    SalesOrderDTO getById(Long id);
    
    /**
     * Get Sales Order by code
     */
    SalesOrderDTO getByCode(String code);
    
    /**
     * Get all Sales Orders
     */
    List<SalesOrderDTO> getAll();
    
    /**
     * Get Sales Orders by status
     */
    List<SalesOrderDTO> getByStatus(SalesOrderStatus status);
    
    /**
     * Get Sales Orders ready for goods issue
     */
    List<SalesOrderDTO> getReadyForGoodsIssue();
    
    /**
     * Get Sales Orders pending approval
     */
    List<SalesOrderDTO> getPendingApproval();
    
    /**
     * Get Sales Orders by customer
     */
    List<SalesOrderDTO> getByCustomerId(Long customerId);
    
    /**
     * Get Sales Orders by date range
     */
    List<SalesOrderDTO> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Search Sales Orders
     */
    List<SalesOrderDTO> search(String query);
    
    /**
     * Delete Sales Order (only when OPEN)
     */
    void delete(Long id);
    
    /**
     * Approve or Reject a Sales Order
     * Only Manager or Accountant can perform this action
     */
    SalesOrderDTO processApproval(Long id, ApprovalRequestDTO approvalRequest);
    
    /**
     * Approve a Sales Order
     */
    SalesOrderDTO approve(Long id, Long approvedBy);
    
    /**
     * Reject a Sales Order
     */
    SalesOrderDTO reject(Long id, Long rejectedBy, String reason);
    
    /**
     * Cancel a Sales Order
     */
    SalesOrderDTO cancel(Long id, String reason);
    
    /**
     * Update SO status based on delivery progress
     * Called after goods issue confirmation
     */
    void updateDeliveryStatus(Long soId);
    
    /**
     * Get issue summary for SO items (remaining quantities to issue)
     */
    SalesOrderDTO getIssueSummary(Long soId);
}
