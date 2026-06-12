package com.distribution.service.impl;

import com.distribution.dto.InventoryDTO;
import com.distribution.exception.InventoryException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.InventoryTransaction.TransactionType;
import com.distribution.repository.*;
import com.distribution.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryService
 * 
 * Handles all inventory operations with transactional consistency
 * Uses pessimistic locking for stock updates to prevent race conditions
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepo;
    private final InventoryTransactionRepository transactionRepo;
    private final ProductRepository productRepo;
    private final WarehouseRepository warehouseRepo;

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepo.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(this::toDto)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getByProduct(Long productId) {
        return inventoryRepo.findByProductId(productId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getByWarehouse(Long warehouseId) {
        return inventoryRepo.findByWarehouseId(warehouseId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getAll() {
        return inventoryRepo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getNeedingReorder() {
        return inventoryRepo.findNeedingReorder().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStock(Integer threshold) {
        return inventoryRepo.findLowStock(threshold).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Inventory addStock(Long productId, Long warehouseId, Integer quantity, BigDecimal unitCost,
                              String referenceType, Long referenceId, String referenceCode, Long userId) {
        logger.info("Adding stock: productId={}, warehouseId={}, quantity={}, ref={}-{}", 
            productId, warehouseId, quantity, referenceType, referenceId);

        // Get or create inventory record with pessimistic lock
        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseGet(() -> createInventory(productId, warehouseId));

        // Record quantity before update
        Integer quantityBefore = inventory.getQuantityOnHand();

        // Add stock
        inventory.addStock(quantity, unitCost);

        // Save inventory
        Inventory saved = inventoryRepo.save(inventory);

        // Create transaction record
        createTransaction(
            productId, warehouseId, TransactionType.RECEIPT, quantity, unitCost,
            quantityBefore, saved.getQuantityOnHand(),
            referenceType, referenceId, referenceCode, userId,
            "Stock received from " + referenceType
        );

        logger.info("Stock added successfully. New quantity: {}", saved.getQuantityOnHand());
        return saved;
    }

    @Override
    public Inventory removeStock(Long productId, Long warehouseId, Integer quantity,
                                 String referenceType, Long referenceId, String referenceCode, Long userId) {
        logger.info("Removing stock: productId={}, warehouseId={}, quantity={}, ref={}-{}", 
            productId, warehouseId, quantity, referenceType, referenceId);

        // Get inventory with pessimistic lock
        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseThrow(() -> new InventoryException(
                String.format("No inventory found for product %d at warehouse %d", productId, warehouseId)));

        // Validate sufficient stock
        if (inventory.getQuantityAvailable() < quantity) {
            throw InventoryException.insufficientStock(
                inventory.getProduct().getName(), 
                inventory.getQuantityAvailable(), 
                quantity
            );
        }

        // Record quantity before update
        Integer quantityBefore = inventory.getQuantityOnHand();

        // Remove stock
        inventory.removeStock(quantity);

        // Save inventory
        Inventory saved = inventoryRepo.save(inventory);

        // Create transaction record
        createTransaction(
            productId, warehouseId, TransactionType.ISSUE, quantity, inventory.getAverageCost(),
            quantityBefore, saved.getQuantityOnHand(),
            referenceType, referenceId, referenceCode, userId,
            "Stock issued for " + referenceType
        );

        logger.info("Stock removed successfully. New quantity: {}", saved.getQuantityOnHand());
        return saved;
    }

    @Override
    public Inventory reserveStock(Long productId, Long warehouseId, Integer quantity) {
        logger.info("Reserving stock: productId={}, warehouseId={}, quantity={}", 
            productId, warehouseId, quantity);

        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseThrow(() -> new InventoryException(
                String.format("No inventory found for product %d at warehouse %d", productId, warehouseId)));

        inventory.reserveStock(quantity);
        
        return inventoryRepo.save(inventory);
    }

    @Override
    public Inventory releaseReservedStock(Long productId, Long warehouseId, Integer quantity) {
        logger.info("Releasing reserved stock: productId={}, warehouseId={}, quantity={}", 
            productId, warehouseId, quantity);

        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseThrow(() -> new InventoryException(
                String.format("No inventory found for product %d at warehouse %d", productId, warehouseId)));

        inventory.releaseReservedStock(quantity);
        
        return inventoryRepo.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByProduct(Long productId) {
        return transactionRepo.findByProductIdOrderByTransactionDateDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByWarehouse(Long warehouseId) {
        return transactionRepo.findByWarehouseIdOrderByTransactionDateDesc(warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsByReference(String referenceType, Long referenceId) {
        return transactionRepo.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalAvailableQuantity(Long productId) {
        return inventoryRepo.getTotalAvailableQuantityByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(Long productId, Long warehouseId) {
        return inventoryRepo.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(Inventory::getQuantityAvailable)
            .orElse(0);
    }

    @Override
    public void reserveInventory(Long productId, Long warehouseId, Integer quantity) {
        reserveStock(productId, warehouseId, quantity);
    }

    @Override
    public void releaseReservedInventory(Long productId, Long warehouseId, Integer quantity) {
        try {
            releaseReservedStock(productId, warehouseId, quantity);
        } catch (Exception e) {
            logger.warn("Could not release reserved inventory for product {} at warehouse {}: {}", 
                productId, warehouseId, e.getMessage());
        }
    }

    @Override
    public void decreaseInventory(Long productId, Long warehouseId, Integer quantity,
                                  String referenceType, Long referenceId, String referenceCode) {
        logger.info("Decreasing inventory: productId={}, warehouseId={}, quantity={}, ref={}-{}", 
            productId, warehouseId, quantity, referenceType, referenceId);

        // Get inventory with pessimistic lock
        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseThrow(() -> new InventoryException(
                String.format("No inventory found for product %d at warehouse %d", productId, warehouseId)));

        // Validate sufficient stock
        if (inventory.getQuantityOnHand() < quantity) {
            throw InventoryException.insufficientStock(
                inventory.getProduct().getName(), 
                inventory.getQuantityOnHand(), 
                quantity
            );
        }

        // Record quantity before update
        Integer quantityBefore = inventory.getQuantityOnHand();

        // Decrease stock (on-hand only, available is managed by reservation)
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        inventory.setQuantityAvailable(inventory.getQuantityOnHand() - inventory.getQuantityReserved());
        inventory.setLastIssuedDate(java.time.LocalDateTime.now());

        // Save inventory
        Inventory saved = inventoryRepo.save(inventory);

        // Create transaction record
        createTransaction(
            productId, warehouseId, TransactionType.ISSUE, quantity, inventory.getAverageCost(),
            quantityBefore, saved.getQuantityOnHand(),
            referenceType, referenceId, referenceCode, null,
            "Stock issued for " + referenceType
        );

        logger.info("Inventory decreased successfully. New quantity: {}", saved.getQuantityOnHand());
    }

    @Override
    public void disposeInventory(Long productId, Long warehouseId, Integer quantity,
                                 String referenceType, Long referenceId, String referenceCode,
                                 Long userId, String notes) {
        logger.info("Disposing inventory: productId={}, warehouseId={}, quantity={}, ref={}-{}",
            productId, warehouseId, quantity, referenceType, referenceId);

        // Get inventory with pessimistic lock
        Inventory inventory = inventoryRepo.findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
            .orElseThrow(() -> new InventoryException(
                String.format("No inventory found for product %d at warehouse %d", productId, warehouseId)));

        // Validate sufficient stock
        if (inventory.getQuantityOnHand() < quantity) {
            throw InventoryException.insufficientStock(
                inventory.getProduct().getName(),
                inventory.getQuantityOnHand(),
                quantity
            );
        }

        // Record quantity before update
        Integer quantityBefore = inventory.getQuantityOnHand();

        // Decrease stock (on-hand only, available is managed by reservation)
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        inventory.setQuantityAvailable(inventory.getQuantityOnHand() - inventory.getQuantityReserved());
        inventory.setLastIssuedDate(java.time.LocalDateTime.now());

        Inventory saved = inventoryRepo.save(inventory);

        createTransaction(
            productId, warehouseId, TransactionType.DISPOSAL, quantity, inventory.getAverageCost(),
            quantityBefore, saved.getQuantityOnHand(),
            referenceType, referenceId, referenceCode, userId,
            notes != null ? notes : "Stock disposed"
        );

        logger.info("Inventory disposed successfully. New quantity: {}", saved.getQuantityOnHand());
    }

    // Helper methods
    private Inventory createInventory(Long productId, Long warehouseId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepo.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        return Inventory.builder()
            .product(product)
            .warehouse(warehouse)
            .quantityOnHand(0)
            .quantityReserved(0)
            .quantityAvailable(0)
            .build();
    }

    private void createTransaction(Long productId, Long warehouseId, TransactionType type,
                                   Integer quantity, BigDecimal unitCost,
                                   Integer quantityBefore, Integer quantityAfter,
                                   String referenceType, Long referenceId, String referenceCode,
                                   Long userId, String notes) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepo.findById(warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        InventoryTransaction transaction = InventoryTransaction.builder()
            .product(product)
            .warehouse(warehouse)
            .transactionType(type)
            .quantity(quantity)
            .unitCost(unitCost)
            .totalCost(unitCost != null ? unitCost.multiply(BigDecimal.valueOf(quantity)) : null)
            .quantityBefore(quantityBefore)
            .quantityAfter(quantityAfter)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .referenceCode(referenceCode)
            .createdBy(userId)
            .notes(notes)
            .build();

        transactionRepo.save(transaction);
        logger.debug("Created inventory transaction: {}", transaction.getId());
    }

    @Override
    public InventoryDTO updateReorderLevel(Long inventoryId, Integer reorderLevel, Integer reorderQuantity) {
        Inventory inventory = inventoryRepo.findById(inventoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found: " + inventoryId));

        // Guard against legacy rows with NULL version (avoids Hibernate version-increment NPE on save)
        if (inventory.getVersion() == null) {
            inventory.setVersion(0L);
        }

        inventory.setReorderLevel(reorderLevel);
        if (reorderQuantity != null) {
            inventory.setReorderQuantity(reorderQuantity);
        }

        Inventory saved = inventoryRepo.save(inventory);
        logger.info("Updated reorder level for inventory {}: reorderLevel={}", inventoryId, reorderLevel);
        return toDto(saved);
    }

    private InventoryDTO toDto(Inventory inventory) {
        InventoryDTO dto = InventoryDTO.builder()
            .id(inventory.getId())
            .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
            .productName(inventory.getProduct() != null ? inventory.getProduct().getName() : null)
            .productCode(inventory.getProduct() != null ? inventory.getProduct().getCode() : null)
            .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
            .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
            .warehouseCode(inventory.getWarehouse() != null ? inventory.getWarehouse().getCode() : null)
            .quantityOnHand(inventory.getQuantityOnHand())
            .quantityReserved(inventory.getQuantityReserved())
            .quantityAvailable(inventory.getQuantityAvailable())
            .reorderLevel(inventory.getReorderLevel())
            .reorderQuantity(inventory.getReorderQuantity())
            .averageCost(inventory.getAverageCost())
            .lastReceivedDate(inventory.getLastReceivedDate())
            .lastIssuedDate(inventory.getLastIssuedDate())
            .build();

        dto.computeFields();
        return dto;
    }
}
