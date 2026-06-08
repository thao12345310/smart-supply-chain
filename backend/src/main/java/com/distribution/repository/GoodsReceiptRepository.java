package com.distribution.repository;

import com.distribution.model.GoodsReceipt;
import com.distribution.model.enums.GoodsReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    
    /**
     * Find GR by code
     */
    Optional<GoodsReceipt> findByCode(String code);
    
    /**
     * Find all GRs by purchase order
     */
    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
    
    /**
     * Find all GRs by status
     */
    List<GoodsReceipt> findByStatusOrderByIdDesc(GoodsReceiptStatus status);
    
    /**
     * Find all GRs by warehouse
     */
    List<GoodsReceipt> findByWarehouseId(Long warehouseId);
    
    /**
     * Find GRs by receipt date range
     */
    List<GoodsReceipt> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find GR with all items eagerly loaded
     */
    @Query("SELECT gr FROM GoodsReceipt gr LEFT JOIN FETCH gr.items LEFT JOIN FETCH gr.purchaseOrder LEFT JOIN FETCH gr.warehouse WHERE gr.id = :id")
    Optional<GoodsReceipt> findByIdWithItems(@Param("id") Long id);
    
    /**
     * Find draft GRs that are pending confirmation
     */
    default List<GoodsReceipt> findPendingConfirmation() {
        return findByStatusOrderByIdDesc(GoodsReceiptStatus.DRAFT);
    }
    
    /**
     * Count confirmed GRs for a PO
     */
    @Query("SELECT COUNT(gr) FROM GoodsReceipt gr WHERE gr.purchaseOrder.id = :poId AND gr.status = 'CONFIRMED'")
    long countConfirmedByPurchaseOrderId(@Param("poId") Long purchaseOrderId);
    
    /**
     * Check if GR code already exists
     */
    boolean existsByCode(String code);
}
