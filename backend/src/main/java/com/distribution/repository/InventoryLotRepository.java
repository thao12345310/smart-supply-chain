package com.distribution.repository;

import com.distribution.model.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {

    // FEFO query: lots có thể xuất (chưa hết HSD), sort theo expiry_date ASC (null sau cùng)
    @Query("SELECT l FROM InventoryLot l " +
           "WHERE l.product.id = :productId " +
           "AND l.warehouse.id = :warehouseId " +
           "AND l.quantityRemaining > 0 " +
           "AND (l.expiryDate IS NULL OR l.expiryDate >= CURRENT_DATE) " +
           "ORDER BY (CASE WHEN l.expiryDate IS NULL THEN 1 ELSE 0 END), " +
           "l.expiryDate ASC, l.id ASC")
    List<InventoryLot> findAvailableLotsFEFO(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );

    // Tổng tồn còn lại theo (product, warehouse), không tính lô hết HSD
    @Query("SELECT COALESCE(SUM(l.quantityRemaining), 0) FROM InventoryLot l " +
           "WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId " +
           "AND (l.expiryDate IS NULL OR l.expiryDate >= CURRENT_DATE)")
    BigDecimal sumAvailableQuantity(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );

    // Còn lô nào có tồn (kể cả hết HSD) — để phân biệt "không quản lý lô" với "chỉ còn lô hết HSD"
    @Query("SELECT COUNT(l) > 0 FROM InventoryLot l " +
           "WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId " +
           "AND l.quantityRemaining > 0")
    boolean hasLotsWithStock(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );

    // Lô sắp hết HSD trong khoảng [hôm nay, threshold]
    @Query("SELECT l FROM InventoryLot l " +
           "WHERE l.quantityRemaining > 0 " +
           "AND l.expiryDate IS NOT NULL " +
           "AND l.expiryDate >= CURRENT_DATE " +
           "AND l.expiryDate <= :threshold " +
           "ORDER BY l.expiryDate ASC")
    List<InventoryLot> findExpiringSoon(@Param("threshold") LocalDate threshold);

    // Lô đã hết HSD nhưng còn tồn kho (cảnh báo)
    @Query("SELECT l FROM InventoryLot l " +
           "WHERE l.quantityRemaining > 0 " +
           "AND l.expiryDate IS NOT NULL " +
           "AND l.expiryDate < CURRENT_DATE " +
           "ORDER BY l.expiryDate ASC")
    List<InventoryLot> findExpired();

    // Tất cả lots của (product, warehouse), sort FEFO
    @Query("SELECT l FROM InventoryLot l " +
           "WHERE l.product.id = :productId AND l.warehouse.id = :warehouseId " +
           "ORDER BY CASE WHEN l.expiryDate IS NULL THEN 1 ELSE 0 END, l.expiryDate ASC, l.id ASC")
    List<InventoryLot> findByProductAndWarehouse(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );

    // Tất cả lots với filter tùy chọn
    @Query("SELECT l FROM InventoryLot l " +
           "WHERE (:productId IS NULL OR l.product.id = :productId) " +
           "AND (:warehouseId IS NULL OR l.warehouse.id = :warehouseId) " +
           "ORDER BY CASE WHEN l.expiryDate IS NULL THEN 1 ELSE 0 END, l.expiryDate ASC, l.id ASC")
    List<InventoryLot> findAllFiltered(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId
    );

    // Kiểm tra lot đã tồn tại cho receipt item chưa (idempotency)
    boolean existsBySourceReceiptItemId(Long sourceReceiptItemId);

    List<InventoryLot> findByProductIdAndWarehouseIdOrderByExpiryDateAsc(Long productId, Long warehouseId);
}
