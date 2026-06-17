package com.distribution.integration;

import com.distribution.exception.BusinessException;
import com.distribution.model.Inventory;
import com.distribution.model.InventoryLot;
import com.distribution.model.Product;
import com.distribution.model.Warehouse;
import com.distribution.repository.InventoryLotRepository;
import com.distribution.repository.InventoryRepository;
import com.distribution.repository.ProductRepository;
import com.distribution.repository.WarehouseRepository;
import com.distribution.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử khóa chống lost-update ở CẤP LÔ (InventoryLot).
 *
 * <p>Bối cảnh: {@code InventoryLot.quantityRemaining} KHÔNG có {@code @Version}/{@code @Lock} riêng.
 * Nếu hai phiếu xuất kho cùng (product, warehouse) chạy song song, cả hai có thể cùng đọc một lô,
 * cùng tính "đủ tồn", rồi cùng ghi đè quantityRemaining → mất cập nhật (lost update), tồn lô âm.
 *
 * <p>Bản vá: trước khi đọc/ghi lô, {@code GoodsIssueServiceImpl.confirm()} gọi
 * {@code InventoryService.lockInventoryForUpdate(product, warehouse)} — chiếm khóa
 * PESSIMISTIC_WRITE trên DÒNG Inventory, biến nó thành "cổng" serialize cho toàn bộ thao tác
 * trừ tồn của mặt hàng (gồm cả vòng trừ lô). Test này tái dựng đúng vùng tới hạn đó bằng các
 * bean THẬT (lockInventoryForUpdate + findAvailableLotsFEFO + decreaseInventory) và cho hai
 * thread cùng xuất 6 đơn vị từ tồn 10.
 *
 * <p>Kỳ vọng (đã serialize, không lost-update):
 * <ul>
 *   <li>đúng MỘT thread thành công, một thread thất bại (thiếu tồn / không lấy được khóa);</li>
 *   <li>tồn còn lại của lô = 4 (10 − 6), KHÔNG phải −2;</li>
 *   <li>tồn on-hand = 4, KHÔNG phải −2.</li>
 * </ul>
 *
 * <p>Bỏ lời gọi {@code lockInventoryForUpdate} ở vùng tới hạn dưới đây sẽ tái xuất hiện race và
 * test này hỏng — đó chính là bằng chứng cho thấy khóa là cần thiết.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyLotTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private InventoryLotRepository inventoryLotRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private PlatformTransactionManager txManager;

    private TransactionTemplate txTemplate;

    private Long productId;
    private Long warehouseId;
    private Long inventoryId;
    private Long lotId;

    private static final int STARTING_STOCK = 10;
    private static final int ISSUE_QTY = 6; // 6 + 6 = 12 > 10 → chỉ một bên có thể thành công

    @BeforeEach
    void setUp() {
        txTemplate = new TransactionTemplate(txManager);

        // Mã duy nhất theo lần chạy để mỗi test độc lập, không phụ thuộc/đụng dữ liệu cũ trong H2.
        String tag = "CONC-" + System.nanoTime();

        Product product = productRepository.save(Product.builder()
            .code("P-" + tag)
            .name("Concurrency Test Product")
            .build());

        Warehouse warehouse = warehouseRepository.save(Warehouse.builder()
            .code("W-" + tag)
            .name("Concurrency Test Warehouse")
            .build());

        Inventory inventory = inventoryRepository.save(Inventory.builder()
            .product(product)
            .warehouse(warehouse)
            .quantityOnHand(STARTING_STOCK)
            .quantityReserved(0)
            .quantityAvailable(STARTING_STOCK)
            .build());

        InventoryLot lot = inventoryLotRepository.save(InventoryLot.builder()
            .product(product)
            .warehouse(warehouse)
            .lotNumber("LOT-" + tag)
            .expiryDate(LocalDate.now().plusMonths(6))
            .quantityReceived(BigDecimal.valueOf(STARTING_STOCK))
            .quantityRemaining(BigDecimal.valueOf(STARTING_STOCK))
            .build());

        productId = product.getId();
        warehouseId = warehouse.getId();
        inventoryId = inventory.getId();
        lotId = lot.getId();
    }

    @Test
    void twoConcurrentIssues_doNotLoseLotUpdate() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);

        // Mỗi worker: chờ tín hiệu rồi cùng lúc xuất ISSUE_QTY. Trả về null nếu thành công,
        // trả về Throwable nếu thất bại (để test tự đếm, không làm hỏng thread).
        Callable<Throwable> worker = () -> {
            ready.countDown();
            go.await();
            try {
                issue(ISSUE_QTY);
                return null;
            } catch (Throwable t) {
                return t;
            }
        };

        Future<Throwable> f1 = pool.submit(worker);
        Future<Throwable> f2 = pool.submit(worker);

        ready.await(10, TimeUnit.SECONDS); // hai thread đã sẵn sàng
        go.countDown();                    // bắn cùng lúc

        Throwable r1 = f1.get(30, TimeUnit.SECONDS);
        Throwable r2 = f2.get(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        long successes = Stream.of(r1, r2).filter(Objects::isNull).count();
        long failures = Stream.of(r1, r2).filter(Objects::nonNull).count();

        assertThat(successes)
            .as("Đúng một phiếu xuất được phép thành công khi tồn chỉ đủ cho một")
            .isEqualTo(1);
        assertThat(failures)
            .as("Phiếu xuất còn lại phải thất bại (thiếu tồn / không lấy được khóa)")
            .isEqualTo(1);

        Inventory inv = inventoryRepository.findById(inventoryId).orElseThrow();
        assertThat(inv.getQuantityOnHand())
            .as("On-hand phải = 4 (10 − 6), không âm → không lost-update ở tồn tổng")
            .isEqualTo(STARTING_STOCK - ISSUE_QTY);

        InventoryLot lot = inventoryLotRepository.findById(lotId).orElseThrow();
        assertThat(lot.getQuantityRemaining())
            .as("Tồn lô phải = 4 (10 − 6), không phải −2 → khóa đã chống lost-update cấp lô")
            .isEqualByComparingTo("4");
    }

    /**
     * Tái dựng đúng vùng tới hạn của {@code GoodsIssueServiceImpl.confirm()} cho MỘT mặt hàng,
     * trong một transaction độc lập (mỗi thread một transaction).
     */
    private void issue(int qty) {
        txTemplate.executeWithoutResult(status -> {
            // (1) Cổng serialize: khóa PESSIMISTIC_WRITE dòng Inventory, giữ tới hết transaction.
            inventoryService.lockInventoryForUpdate(productId, warehouseId);

            // (2) Đọc lô theo FEFO + kiểm tra đủ tồn (giống confirm()).
            List<InventoryLot> lots = inventoryLotRepository.findAvailableLotsFEFO(productId, warehouseId);
            BigDecimal totalAvailable = lots.stream()
                .map(InventoryLot::getQuantityRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal need = BigDecimal.valueOf(qty);
            if (totalAvailable.compareTo(need) < 0) {
                throw new BusinessException("Không đủ tồn theo lô. Cần: " + qty + ", còn: " + totalAvailable);
            }

            // (3) Trừ theo FEFO (read-modify-write trên quantityRemaining — điểm dễ lost-update).
            BigDecimal stillToIssue = need;
            for (InventoryLot lot : lots) {
                if (stillToIssue.signum() <= 0) break;
                BigDecimal take = stillToIssue.min(lot.getQuantityRemaining());
                lot.setQuantityRemaining(lot.getQuantityRemaining().subtract(take));
                inventoryLotRepository.save(lot);
                stillToIssue = stillToIssue.subtract(take);
            }

            // (4) Trừ tồn tổng — decreaseInventory dùng lại đúng khóa và tự kiểm tra on-hand.
            inventoryService.decreaseInventory(productId, warehouseId, qty, "TEST_CONCURRENCY", null, "TEST");
        });
    }
}
