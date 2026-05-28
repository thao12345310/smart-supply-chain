package com.distribution.repository;

import com.distribution.model.PurchaseOrder;
import com.distribution.model.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    /**
     * Find PO by code
     */
    Optional<PurchaseOrder> findByCode(String code);
    
    /**
     * Find all POs by status
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = :status ORDER BY po.id DESC")
    List<PurchaseOrder> findByStatus(@Param("status") PurchaseOrderStatus status);

    /**
     * Find all POs by supplier
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId ORDER BY po.id DESC")
    List<PurchaseOrder> findBySupplierId(@Param("supplierId") Long supplierId);

    /**
     * Find all POs by warehouse
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.warehouse.id = :warehouseId ORDER BY po.id DESC")
    List<PurchaseOrder> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * Find POs created between dates
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdDate BETWEEN :startDate AND :endDate ORDER BY po.id DESC")
    List<PurchaseOrder> findByCreatedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find approved POs that are ready for goods receipt
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status IN (:statuses) ORDER BY po.id DESC")
    List<PurchaseOrder> findByStatusIn(@Param("statuses") List<PurchaseOrderStatus> statuses);
    
    /**
     * Find POs that are pending approval
     */
    default List<PurchaseOrder> findPendingApproval() {
        return findByStatus(PurchaseOrderStatus.ORDER_OPEN);
    }
    
    /**
     * Find POs that can receive goods (APPROVED or PARTIALLY_RECEIVED)
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status IN ('ORDER_APPROVED', 'ORDER_PARTIALLY_RECEIVED') ORDER BY po.id DESC")
    List<PurchaseOrder> findReadyForGoodsReceipt();
    
    /**
     * Find PO with all items eagerly loaded
     */
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.items LEFT JOIN FETCH po.supplier LEFT JOIN FETCH po.warehouse WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") Long id);
    
    /**
     * Find PO with goods receipts
     */
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.goodsReceipts WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithGoodsReceipts(@Param("id") Long id);
    
    /**
     * Check if PO code already exists
     */
    boolean existsByCode(String code);
    
    /**
     * Count POs by status
     */
    long countByStatus(PurchaseOrderStatus status);
}
