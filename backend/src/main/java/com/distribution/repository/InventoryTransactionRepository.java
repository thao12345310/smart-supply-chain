package com.distribution.repository;

import com.distribution.model.InventoryTransaction;
import com.distribution.model.InventoryTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    /**
     * Find transactions by product
     */
    List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(Long productId);
    
    /**
     * Find transactions by warehouse
     */
    List<InventoryTransaction> findByWarehouseIdOrderByTransactionDateDesc(Long warehouseId);
    
    /**
     * Find transactions by type
     */
    List<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionType);
    
    /**
     * Find transactions by reference
     */
    List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    /**
     * Find transactions by date range
     */
    List<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
        LocalDateTime startDate, LocalDateTime endDate
    );
    
    /**
     * Find transactions by product and date range
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.product.id = :productId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findByProductAndDateRange(
        @Param("productId") Long productId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions by product and warehouse
     */
    List<InventoryTransaction> findByProductIdAndWarehouseIdOrderByTransactionDateDesc(
        Long productId, Long warehouseId
    );
}
