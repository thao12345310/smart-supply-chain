package com.distribution.integration;

import com.distribution.model.Inventory;
import com.distribution.model.InventoryLot;
import com.distribution.model.Product;
import com.distribution.model.Warehouse;
import com.distribution.repository.InventoryLotRepository;
import com.distribution.repository.InventoryRepository;
import com.distribution.repository.ProductRepository;
import com.distribution.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC01 — Kiểm thử cấp phát theo FEFO (First-Expired-First-Out) khi xuất kho.
 *
 * <p>Bối cảnh: một mặt hàng tồn ở hai lô khác hạn dùng. Yêu cầu nghiệp vụ là lô
 * HẾT HẠN SỚM HƠN phải được xuất trước để giảm hao hụt do quá hạn.
 *
 * <p>Test dựng hai lô (cận hạn 1 tháng / xa hạn 6 tháng) rồi: (i) gọi truy vấn THẬT
 * {@code InventoryLotRepository.findAvailableLotsFEFO} và khẳng định thứ tự trả về
 * đúng FEFO (lô cận hạn đứng trước); (ii) tái dựng đúng vòng trừ tham lam của
 * {@code GoodsIssueServiceImpl.confirm()} cho lượng xuất 6 và khẳng định lô cận hạn
 * bị tiêu thụ hết trước, phần thiếu mới lấy sang lô xa hạn.
 */
@SpringBootTest
@ActiveProfiles("test")
class FefoAllocationTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private InventoryLotRepository inventoryLotRepository;

    private Long productId;
    private Long warehouseId;
    private Long nearLotId;
    private Long farLotId;

    @BeforeEach
    void setUp() {
        String tag = "FEFO-" + System.nanoTime();

        Product product = productRepository.save(Product.builder()
            .code("P-" + tag).name("FEFO Test Product").build());
        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("W-" + tag).name("FEFO Test Warehouse").build());

        inventoryRepository.save(Inventory.builder()
            .product(product).warehouse(warehouse)
            .quantityOnHand(14).quantityReserved(0).quantityAvailable(14)
            .build());

        // Lô cận hạn: ít hàng (4), hết hạn sau 1 tháng → phải xuất trước.
        InventoryLot near = inventoryLotRepository.save(InventoryLot.builder()
            .product(product).warehouse(warehouse)
            .lotNumber("LOT-NEAR-" + tag)
            .expiryDate(LocalDate.now().plusMonths(1))
            .quantityReceived(BigDecimal.valueOf(4))
            .quantityRemaining(BigDecimal.valueOf(4))
            .build());
        // Lô xa hạn: nhiều hàng (10), hết hạn sau 6 tháng.
        InventoryLot far = inventoryLotRepository.save(InventoryLot.builder()
            .product(product).warehouse(warehouse)
            .lotNumber("LOT-FAR-" + tag)
            .expiryDate(LocalDate.now().plusMonths(6))
            .quantityReceived(BigDecimal.valueOf(10))
            .quantityRemaining(BigDecimal.valueOf(10))
            .build());

        productId = product.getId();
        warehouseId = warehouse.getId();
        nearLotId = near.getId();
        farLotId = far.getId();
    }

    @Test
    void fefoQuery_returnsEarliestExpiryFirst() {
        List<InventoryLot> lots = inventoryLotRepository.findAvailableLotsFEFO(productId, warehouseId);

        assertThat(lots).hasSize(2);
        assertThat(lots.get(0).getId())
            .as("Lô cận hạn (1 tháng) phải đứng trước lô xa hạn (6 tháng)")
            .isEqualTo(nearLotId);
        assertThat(lots.get(1).getId()).isEqualTo(farLotId);
    }

    @Test
    void issue_consumesEarliestExpiryLotFirst() {
        // Tái dựng đúng vòng trừ tham lam theo FEFO của confirm() cho lượng xuất 6.
        BigDecimal remaining = BigDecimal.valueOf(6);
        for (InventoryLot lot : inventoryLotRepository.findAvailableLotsFEFO(productId, warehouseId)) {
            if (remaining.signum() <= 0) break;
            BigDecimal take = remaining.min(lot.getQuantityRemaining());
            lot.setQuantityRemaining(lot.getQuantityRemaining().subtract(take));
            inventoryLotRepository.save(lot);
            remaining = remaining.subtract(take);
        }

        InventoryLot near = inventoryLotRepository.findById(nearLotId).orElseThrow();
        InventoryLot far = inventoryLotRepository.findById(farLotId).orElseThrow();
        assertThat(near.getQuantityRemaining())
            .as("Lô cận hạn (4) phải bị xuất hết trước")
            .isEqualByComparingTo("0");
        assertThat(far.getQuantityRemaining())
            .as("Chỉ phần thiếu (6−4=2) mới lấy từ lô xa hạn → còn 8")
            .isEqualByComparingTo("8");
    }
}
