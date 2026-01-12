package com.distribution.repository;

import com.distribution.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * Find inventory by product and warehouse
     */
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    
    /**
     * Find inventory by product and warehouse with pessimistic lock for updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
    Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
        @Param("productId") Long productId, 
        @Param("warehouseId") Long warehouseId
    );
    
    /**
     * Find all inventory by product
     */
    List<Inventory> findByProductId(Long productId);
    
    /**
     * Find all inventory by warehouse
     */
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    /**
     * Find inventory items that need reorder
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.reorderLevel AND i.reorderLevel IS NOT NULL")
    List<Inventory> findNeedingReorder();
    
    /**
     * Find low stock items (below a threshold)
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable < :threshold")
    List<Inventory> findLowStock(@Param("threshold") Integer threshold);
    
    /**
     * Get total quantity on hand for a product across all warehouses
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalQuantityOnHandByProductId(@Param("productId") Long productId);
    
    /**
     * Get total available quantity for a product across all warehouses
     */
    @Query("SELECT COALESCE(SUM(i.quantityAvailable), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalAvailableQuantityByProductId(@Param("productId") Long productId);
}
