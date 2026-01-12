package com.distribution.repository;

import com.distribution.model.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {
    
    /**
     * Find all items by goods receipt
     */
    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);
    
    /**
     * Find all items by PO item
     */
    List<GoodsReceiptItem> findByPurchaseOrderItemId(Long purchaseOrderItemId);
    
    /**
     * Find all items by product
     */
    List<GoodsReceiptItem> findByProductId(Long productId);
    
    /**
     * Calculate total received quantity for a PO item across all confirmed GRs
     */
    @Query("SELECT COALESCE(SUM(gri.acceptedQuantity), 0) FROM GoodsReceiptItem gri " +
           "WHERE gri.purchaseOrderItem.id = :poItemId AND gri.goodsReceipt.status = 'CONFIRMED'")
    Integer sumAcceptedQuantityByPurchaseOrderItemId(@Param("poItemId") Long purchaseOrderItemId);
}
