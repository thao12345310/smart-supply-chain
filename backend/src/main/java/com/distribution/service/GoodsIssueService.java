package com.distribution.service;

import com.distribution.dto.GoodsIssueDTO;
import com.distribution.model.enums.GoodsIssueStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Goods Issue (outbound delivery) operations
 */
public interface GoodsIssueService {
    
    /**
     * Create a new Goods Issue
     * Validates that quantities don't exceed remaining quantities
     * Sets initial status to DRAFT
     */
    GoodsIssueDTO create(GoodsIssueDTO dto);
    
    /**
     * Update an existing Goods Issue
     * Only allowed when status is DRAFT
     */
    GoodsIssueDTO update(Long id, GoodsIssueDTO dto);
    
    /**
     * Get Goods Issue by ID
     */
    GoodsIssueDTO getById(Long id);
    
    /**
     * Get Goods Issue by code
     */
    GoodsIssueDTO getByCode(String code);
    
    /**
     * Get all Goods Issues
     */
    List<GoodsIssueDTO> getAll();
    
    /**
     * Get Goods Issues by status
     */
    List<GoodsIssueDTO> getByStatus(GoodsIssueStatus status);
    
    /**
     * Get Goods Issues by Sales Order
     */
    List<GoodsIssueDTO> getBySalesOrderId(Long salesOrderId);
    
    /**
     * Get Goods Issues by date range
     */
    List<GoodsIssueDTO> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get draft Goods Issues
     */
    List<GoodsIssueDTO> getDraft();
    
    /**
     * Search Goods Issues
     */
    List<GoodsIssueDTO> search(String query);
    
    /**
     * Delete Goods Issue (only when DRAFT)
     */
    void delete(Long id);
    
    /**
     * Confirm Goods Issue
     * - Validates inventory availability
     * - Decreases inventory
     * - Updates SO item delivered quantities
     * - Creates invoice
     * - Updates SO status
     */
    GoodsIssueDTO confirm(Long id, Long confirmedBy);
    
    /**
     * Cancel Goods Issue (only when DRAFT)
     */
    GoodsIssueDTO cancel(Long id);

    /**
     * Hoàn hàng về kho khi một vận đơn (gắn với phiếu xuất này) giao THẤT BẠI.
     * Tăng lại tồn on-hand + lô FEFO và giảm số lượng đã giao của đơn bán,
     * sau đó tính lại trạng thái giao của đơn bán. Bỏ qua nếu không tìm thấy phiếu xuất.
     */
    void restockFromFailedDelivery(String goodsIssueCode);

    /**
     * Trừ kho lại khi sửa kết quả giao từ THẤT BẠI -> THÀNH CÔNG (đảo ngược restock).
     * Ném lỗi nếu tồn không đủ để ghi nhận lại.
     */
    void reissueAfterCorrection(String goodsIssueCode);
}
