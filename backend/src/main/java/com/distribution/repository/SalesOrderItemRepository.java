package com.distribution.repository;

import com.distribution.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {
    
    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);
    
    List<SalesOrderItem> findByProductId(Long productId);
    
    @Query("SELECT soi FROM SalesOrderItem soi WHERE soi.salesOrder.id = :salesOrderId AND soi.deliveredQuantity < soi.quantity")
    List<SalesOrderItem> findPendingDeliveryBySalesOrderId(Long salesOrderId);
    
    @Query("SELECT SUM(soi.quantity) FROM SalesOrderItem soi WHERE soi.salesOrder.id = :salesOrderId")
    Integer getTotalQuantityBySalesOrderId(Long salesOrderId);
    
    @Query("SELECT SUM(soi.deliveredQuantity) FROM SalesOrderItem soi WHERE soi.salesOrder.id = :salesOrderId")
    Integer getTotalDeliveredQuantityBySalesOrderId(Long salesOrderId);
}
