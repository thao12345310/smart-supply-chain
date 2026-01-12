package com.distribution.service;

import com.distribution.dto.GoodsReceiptDTO;
import com.distribution.model.enums.GoodsReceiptStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Goods Receipt operations
 */
public interface GoodsReceiptService {
    
    /**
     * Create a new Goods Receipt for a Purchase Order
     * Validates that PO is approved and quantities don't exceed ordered
     */
    GoodsReceiptDTO create(GoodsReceiptDTO dto);
    
    /**
     * Update a draft Goods Receipt
     * Only allowed when status is DRAFT
     */
    GoodsReceiptDTO update(Long id, GoodsReceiptDTO dto);
    
    /**
     * Get Goods Receipt by ID
     */
    GoodsReceiptDTO getById(Long id);
    
    /**
     * Get Goods Receipt by code
     */
    GoodsReceiptDTO getByCode(String code);
    
    /**
     * Get all Goods Receipts
     */
    List<GoodsReceiptDTO> getAll();
    
    /**
     * Get Goods Receipts by status
     */
    List<GoodsReceiptDTO> getByStatus(GoodsReceiptStatus status);
    
    /**
     * Get Goods Receipts by Purchase Order
     */
    List<GoodsReceiptDTO> getByPurchaseOrderId(Long purchaseOrderId);
    
    /**
     * Get Goods Receipts by warehouse
     */
    List<GoodsReceiptDTO> getByWarehouseId(Long warehouseId);
    
    /**
     * Get Goods Receipts by date range
     */
    List<GoodsReceiptDTO> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get pending Goods Receipts (draft status)
     */
    List<GoodsReceiptDTO> getPendingConfirmation();
    
    /**
     * Confirm Goods Receipt
     * - Updates PO item received quantities
     * - Updates inventory
     * - Creates inventory transaction records
     */
    GoodsReceiptDTO confirm(Long id, Long confirmedBy);
    
    /**
     * Cancel Goods Receipt (only for draft status)
     */
    GoodsReceiptDTO cancel(Long id);
    
    /**
     * Delete Goods Receipt (only for draft status)
     */
    void delete(Long id);
    
    /**
     * Get receiving summary for a Purchase Order
     * Returns ordered vs received quantities per item
     */
    GoodsReceiptDTO getReceivingSummary(Long purchaseOrderId);
}
