package com.distribution.integration;

import com.distribution.model.Inventory;
import com.distribution.model.Product;
import com.distribution.model.Warehouse;
import com.distribution.repository.InventoryRepository;
import com.distribution.repository.ProductRepository;
import com.distribution.repository.WarehouseRepository;
import com.distribution.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TC02 — Kiểm thử GIỮ CHỖ (reservation) tồn kho khi duyệt đơn bán.
 *
 * <p>Khi duyệt một đơn bán, {@code SalesOrderServiceImpl.approve()} kiểm tra tồn KHẢ DỤNG
 * rồi gọi {@code InventoryService.reserveInventory(...)}. Test tác động trực tiếp lên đúng
 * primitive đó: giữ chỗ làm tăng tồn-giữ-chỗ và giảm tồn-khả-dụng, trong khi tồn-thực
 * (on-hand) GIỮ NGUYÊN cho tới khi thực xuất kho — nhờ vậy hệ thống không bán quá số lượng
 * thực có. Trường hợp giữ chỗ vượt tồn khả dụng phải bị từ chối.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReservationTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;

    private Long productId;
    private Long warehouseId;
    private Long inventoryId;

    @BeforeEach
    void setUp() {
        String tag = "RSV-" + System.nanoTime();
        Product product = productRepository.save(Product.builder()
            .code("P-" + tag).name("Reservation Test Product").build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("W-" + tag).name("Reservation Test Warehouse").build());
        Inventory inventory = inventoryRepository.save(Inventory.builder()
            .product(product).warehouse(warehouse)
            .quantityOnHand(100).quantityReserved(0).quantityAvailable(100)
            .build());
        productId = product.getId();
        warehouseId = warehouse.getId();
        inventoryId = inventory.getId();
    }

    @Test
    void reserve_increasesReserved_reducesAvailable_onHandUnchanged() {
        inventoryService.reserveInventory(productId, warehouseId, 30);

        Inventory inv = inventoryRepository.findById(inventoryId).orElseThrow();
        assertThat(inv.getQuantityReserved()).as("Tồn giữ chỗ tăng lên 30").isEqualTo(30);
        assertThat(inv.getQuantityAvailable()).as("Tồn khả dụng = 100 − 30").isEqualTo(70);
        assertThat(inv.getQuantityOnHand()).as("Tồn thực không đổi cho tới khi xuất kho").isEqualTo(100);
        assertThat(inventoryService.getAvailableQuantity(productId, warehouseId)).isEqualTo(70);
    }

    @Test
    void reserveBeyondAvailable_isRejected() {
        assertThatThrownBy(() -> inventoryService.reserveInventory(productId, warehouseId, 1000))
            .as("Không thể giữ chỗ vượt tồn khả dụng → chống bán quá")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void release_returnsReservedToAvailable() {
        inventoryService.reserveInventory(productId, warehouseId, 30);
        inventoryService.releaseReservedInventory(productId, warehouseId, 10);

        Inventory inv = inventoryRepository.findById(inventoryId).orElseThrow();
        assertThat(inv.getQuantityReserved()).as("Giải phóng 10 → còn giữ chỗ 20").isEqualTo(20);
        assertThat(inv.getQuantityAvailable()).as("Tồn khả dụng = 100 − 20").isEqualTo(80);
    }
}
