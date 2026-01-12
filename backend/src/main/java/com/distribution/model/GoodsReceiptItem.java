package com.distribution.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Goods Receipt Item Entity
 * 
 * Links goods receipt to specific PO items and tracks received quantities
 */
@Entity
@Table(name = "goods_receipt_item", indexes = {
    @Index(name = "idx_gri_goods_receipt", columnList = "goods_receipt_id"),
    @Index(name = "idx_gri_po_item", columnList = "purchase_order_item_id"),
    @Index(name = "idx_gri_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsReceiptItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    @JsonIgnore
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "purchaseOrder"})
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "accepted_quantity", nullable = false)
    private Integer acceptedQuantity;

    @Column(name = "rejected_quantity")
    @Builder.Default
    private Integer rejectedQuantity = 0;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 50)
    private String unit;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(length = 255)
    private String notes;

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
        if (rejectedQuantity == null) {
            rejectedQuantity = 0;
        }
        
        // Accepted = Received - Rejected
        if (receivedQuantity != null) {
            this.acceptedQuantity = receivedQuantity - rejectedQuantity;
        }
        
        // Calculate total based on accepted quantity
        if (unitPrice != null && acceptedQuantity != null) {
            this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(acceptedQuantity));
        }
    }

    /**
     * Validate that received quantity doesn't exceed remaining ordered quantity
     * @param remainingQuantity The remaining quantity that can be received for this PO item
     */
    public void validateReceivedQuantity(int remainingQuantity) {
        if (receivedQuantity > remainingQuantity) {
            throw new IllegalArgumentException(
                String.format("Received quantity (%d) exceeds remaining ordered quantity (%d)",
                    receivedQuantity, remainingQuantity)
            );
        }
    }
}
