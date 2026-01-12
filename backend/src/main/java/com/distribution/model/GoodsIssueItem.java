package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Goods Issue Item Entity
 * 
 * Represents a line item in a Goods Issue (outbound delivery)
 */
@Entity
@Table(name = "goods_issue_item", indexes = {
    @Index(name = "idx_gii_goods_issue", columnList = "goods_issue_id"),
    @Index(name = "idx_gii_so_item", columnList = "sales_order_item_id"),
    @Index(name = "idx_gii_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GoodsIssueItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity;

    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 50)
    private String unit;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(length = 500)
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_issue_id", nullable = false)
    @JsonIgnore
    private GoodsIssue goodsIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "salesOrder"})
    private SalesOrderItem salesOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
        if (issuedQuantity != null && unitPrice != null) {
            totalAmount = unitPrice.multiply(BigDecimal.valueOf(issuedQuantity));
        }
    }
}
