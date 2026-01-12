package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.distribution.model.enums.GoodsReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Goods Receipt (GR) Entity - Represents a shipment/delivery of goods for a Purchase Order
 * 
 * Business Rules:
 * - One PO can have multiple Goods Receipts (partial receiving)
 * - GR is created by Warehouse Staff
 * - Confirmation updates inventory and PO received quantities
 * - Cannot receive more than ordered quantity
 */
@Entity
@Table(name = "goods_receipt", indexes = {
    @Index(name = "idx_gr_code", columnList = "code"),
    @Index(name = "idx_gr_status", columnList = "status"),
    @Index(name = "idx_gr_purchase_order", columnList = "purchase_order_id"),
    @Index(name = "idx_gr_receipt_date", columnList = "receipt_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsReceipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoodsReceiptStatus status;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "delivery_note_number", length = 100)
    private String deliveryNoteNumber;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;

    // Audit fields
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "goodsReceipts"})
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsReceiptItem> items = new ArrayList<>();

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = GoodsReceiptStatus.DRAFT;
        }
        if (receiptDate == null) {
            receiptDate = LocalDate.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add item to goods receipt
     */
    public void addItem(GoodsReceiptItem item) {
        items.add(item);
        item.setGoodsReceipt(this);
        recalculateTotal();
    }

    /**
     * Remove item from goods receipt
     */
    public void removeItem(GoodsReceiptItem item) {
        items.remove(item);
        item.setGoodsReceipt(null);
        recalculateTotal();
    }

    /**
     * Recalculate total amount from items
     */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(GoodsReceiptItem::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generate GR code
     */
    public static String generateCode() {
        return "GR-" + System.currentTimeMillis();
    }
}
