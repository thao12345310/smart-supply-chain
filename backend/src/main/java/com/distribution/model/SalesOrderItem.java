package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Sales Order Item Entity
 * 
 * Represents a line item in a Sales Order
 */
@Entity
@Table(name = "sales_order_item", indexes = {
    @Index(name = "idx_soi_sales_order", columnList = "sales_order_id"),
    @Index(name = "idx_soi_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "delivered_quantity")
    @Builder.Default
    private Integer deliveredQuantity = 0;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "tax_percent", precision = 5, scale = 2)
    private BigDecimal taxPercent;

    @Column(name = "amount_before_tax", precision = 15, scale = 2)
    private BigDecimal amountBeforeTax;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonIgnore
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
        if (deliveredQuantity == null) {
            deliveredQuantity = 0;
        }
        
        if (quantity != null && unitPrice != null) {
            // Calculate base amount
            amountBeforeTax = unitPrice.multiply(BigDecimal.valueOf(quantity));
            
            // Apply discount if any
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = amountBeforeTax.multiply(discountPercent).divide(BigDecimal.valueOf(100));
                amountBeforeTax = amountBeforeTax.subtract(discountAmount);
            }
            
            // Calculate tax if any
            if (taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) > 0) {
                taxAmount = amountBeforeTax.multiply(taxPercent).divide(BigDecimal.valueOf(100));
            } else {
                taxAmount = BigDecimal.ZERO;
            }
            
            // Calculate total
            totalAmount = amountBeforeTax.add(taxAmount);
        }
    }

    /**
     * Get remaining quantity to deliver
     */
    public int getRemainingQuantity() {
        int delivered = deliveredQuantity != null ? deliveredQuantity : 0;
        return quantity - delivered;
    }

    /**
     * Check if item is fully delivered
     */
    public boolean isFullyDelivered() {
        return getRemainingQuantity() <= 0;
    }

    /**
     * Update delivered quantity (add to existing)
     */
    public void addDeliveredQuantity(int issued) {
        if (deliveredQuantity == null) {
            deliveredQuantity = 0;
        }
        deliveredQuantity += issued;
    }
}
