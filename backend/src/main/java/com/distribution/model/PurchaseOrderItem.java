package com.distribution.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Purchase Order Item Entity
 * 
 * Tracks ordered quantity and received quantity for partial receiving support
 */
@Entity
@Table(name = "purchase_order_item", indexes = {
    @Index(name = "idx_poi_purchase_order", columnList = "purchase_order_id"),
    @Index(name = "idx_poi_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "received_quantity", nullable = false)
    @Builder.Default
    private Integer receivedQuantity = 0;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "cost_before_tax", precision = 15, scale = 2)
    private BigDecimal costBeforeTax;

    @Column(name = "amount_before_tax", precision = 15, scale = 2)
    private BigDecimal amountBeforeTax;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 255)
    private String notes;

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
        if (unitPrice != null && quantity != null) {
            this.amountBeforeTax = unitPrice.multiply(BigDecimal.valueOf(quantity));
            
            // Calculate total with tax if applicable
            if (taxAmount != null) {
                this.totalAmount = amountBeforeTax.add(taxAmount);
            } else {
                this.totalAmount = amountBeforeTax;
            }
        }
        
        if (receivedQuantity == null) {
            receivedQuantity = 0;
        }
    }

    /**
     * Get remaining quantity to be received
     */
    public Integer getRemainingQuantity() {
        return quantity - (receivedQuantity != null ? receivedQuantity : 0);
    }

    /**
     * Check if this item is fully received
     */
    public boolean isFullyReceived() {
        return receivedQuantity != null && receivedQuantity >= quantity;
    }

    /**
     * Check if this item is partially received
     */
    public boolean isPartiallyReceived() {
        return receivedQuantity != null && receivedQuantity > 0 && receivedQuantity < quantity;
    }

    /**
     * Add received quantity and validate against ordered quantity
     * @throws IllegalArgumentException if total received exceeds ordered quantity
     */
    public void addReceivedQuantity(int additionalQuantity) {
        int newTotal = (receivedQuantity != null ? receivedQuantity : 0) + additionalQuantity;
        if (newTotal > quantity) {
            throw new IllegalArgumentException(
                String.format("Cannot receive %d units. Would exceed ordered quantity (%d ordered, %d already received)",
                    additionalQuantity, quantity, receivedQuantity)
            );
        }
        this.receivedQuantity = newTotal;
    }
}
