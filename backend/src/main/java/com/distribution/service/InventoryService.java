package com.distribution.service;

import com.distribution.dto.InventoryDTO;
import com.distribution.model.Inventory;
import com.distribution.model.InventoryTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Inventory operations
 */
public interface InventoryService {
    
    /**
     * Get inventory for a product at a specific warehouse
     */
    InventoryDTO getByProductAndWarehouse(Long productId, Long warehouseId);
    
    /**
     * Get all inventory for a product across all warehouses
     */
    List<InventoryDTO> getByProduct(Long productId);
    
    /**
     * Get all inventory at a specific warehouse
     */
    List<InventoryDTO> getByWarehouse(Long warehouseId);
    
    /**
     * Get all inventory
     */
    List<InventoryDTO> getAll();
    
    /**
     * Get inventory items that need reorder
     */
    List<InventoryDTO> getNeedingReorder();
    
    /**
     * Get low stock items
     */
    List<InventoryDTO> getLowStock(Integer threshold);
    
    /**
     * Add stock from goods receipt
     * Creates inventory transaction record
     * @return Updated inventory
     */
    Inventory addStock(Long productId, Long warehouseId, Integer quantity, BigDecimal unitCost,
                       String referenceType, Long referenceId, String referenceCode, Long userId);
    
    /**
     * Remove stock (for sales, transfers, etc.)
     * Creates inventory transaction record
     * @return Updated inventory
     */
    Inventory removeStock(Long productId, Long warehouseId, Integer quantity,
                          String referenceType, Long referenceId, String referenceCode, Long userId);
    
    /**
     * Reserve stock for pending orders
     */
    Inventory reserveStock(Long productId, Long warehouseId, Integer quantity);
    
    /**
     * Release reserved stock
     */
    Inventory releaseReservedStock(Long productId, Long warehouseId, Integer quantity);
    
    /**
     * Get inventory transactions for a product
     */
    List<InventoryTransaction> getTransactionsByProduct(Long productId);
    
    /**
     * Get inventory transactions for a warehouse
     */
    List<InventoryTransaction> getTransactionsByWarehouse(Long warehouseId);
    
    /**
     * Get inventory transactions by reference
     */
    List<InventoryTransaction> getTransactionsByReference(String referenceType, Long referenceId);
    
    /**
     * Get total available quantity for a product across all warehouses
     */
    Integer getTotalAvailableQuantity(Long productId);
    
    /**
     * Get available quantity for a product at a specific warehouse
     * Available = On Hand - Reserved
     */
    Integer getAvailableQuantity(Long productId, Long warehouseId);
    
    /**
     * Reserve inventory for a sales order
     * Increases reserved quantity, decreases available quantity
     */
    void reserveInventory(Long productId, Long warehouseId, Integer quantity);
    
    /**
     * Release reserved inventory (when order is cancelled or goods are issued)
     * Decreases reserved quantity, increases available quantity
     */
    void releaseReservedInventory(Long productId, Long warehouseId, Integer quantity);
    
    /**
     * Decrease inventory when goods are issued
     * Reduces on-hand quantity and creates transaction record
     */
    void decreaseInventory(Long productId, Long warehouseId, Integer quantity,
                           String referenceType, Long referenceId, String referenceCode);
}
