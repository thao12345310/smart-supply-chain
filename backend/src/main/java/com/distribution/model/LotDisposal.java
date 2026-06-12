package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phiếu xuất hủy lô hàng (hết hạn sử dụng / hư hỏng)
 */
@Entity
@Table(name = "lot_disposal", indexes = {
    @Index(name = "idx_lot_disposal_lot", columnList = "lot_id"),
    @Index(name = "idx_lot_disposal_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_lot_disposal_date", columnList = "disposed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LotDisposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private InventoryLot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    // Snapshot tại thời điểm hủy (lô có thể tiếp tục biến động sau này)
    @Column(name = "lot_number", nullable = false, length = 64)
    private String lotNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(length = 255)
    private String reason;

    @Column(name = "disposed_by")
    private Long disposedBy;

    @Column(name = "disposed_at", nullable = false)
    private LocalDateTime disposedAt;

    @PrePersist
    protected void onCreate() {
        if (disposedAt == null) {
            disposedAt = LocalDateTime.now();
        }
    }

    public static String generateCode() {
        return "XH-" + System.currentTimeMillis();
    }
}
