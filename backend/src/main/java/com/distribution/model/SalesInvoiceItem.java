package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Sales Invoice Item Entity
 * 
 * Represents a line item in a Sales Invoice
 */
@Entity
@Table(name = "sales_invoice_item", indexes = {
    @Index(name = "idx_sii_invoice", columnList = "sales_invoice_id"),
    @Index(name = "idx_sii_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesInvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 50)
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2)
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

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    @JsonIgnore
    private SalesInvoice salesInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_issue_item_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "goodsIssue"})
    private GoodsIssueItem goodsIssueItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
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
}
