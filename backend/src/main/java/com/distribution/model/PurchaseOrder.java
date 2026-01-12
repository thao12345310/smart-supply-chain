package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.distribution.model.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Entity
 * 
 * Business Rules:
 * - PO is created by Purchasing Staff with status ORDER_OPEN
 * - Manager or Accountant can approve/reject PO
 * - Approved PO can have multiple Goods Receipts
 * - PO transitions to COMPLETED when all items are fully received
 */
@Entity
@Table(name = "purchase_order", indexes = {
    @Index(name = "idx_po_code", columnList = "code"),
    @Index(name = "idx_po_status", columnList = "status"),
    @Index(name = "idx_po_supplier", columnList = "supplier_id"),
    @Index(name = "idx_po_created_date", columnList = "created_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "order_name", length = 255)
    private String orderName;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "tax_type", length = 50)
    private String taxType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PurchaseOrderStatus status;

    @Column(name = "shipping_cost", precision = 15, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(length = 500)
    private String notes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Audit fields
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GoodsReceipt> goodsReceipts = new ArrayList<>();

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
        if (status == null) {
            status = PurchaseOrderStatus.ORDER_OPEN;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add item to purchase order
     */
    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        recalculateTotal();
    }

    /**
     * Remove item from purchase order
     */
    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        recalculateTotal();
    }

    /**
     * Recalculate total amount from items
     */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (shippingCost != null) {
            this.totalAmount = this.totalAmount.add(shippingCost);
        }
    }

    /**
     * Check if all items have been fully received
     */
    public boolean isFullyReceived() {
        return items.stream().allMatch(item -> 
            item.getReceivedQuantity() >= item.getQuantity()
        );
    }

    /**
     * Check if any items have been partially received
     */
    public boolean isPartiallyReceived() {
        return items.stream().anyMatch(item -> 
            item.getReceivedQuantity() > 0 && item.getReceivedQuantity() < item.getQuantity()
        );
    }
}
