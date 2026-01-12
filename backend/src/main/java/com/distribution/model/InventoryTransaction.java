package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Transaction Entity - Tracks all stock movements
 * 
 * Used for audit trail and inventory reconciliation
 */
@Entity
@Table(name = "inventory_transaction", indexes = {
    @Index(name = "idx_inv_tx_product", columnList = "product_id"),
    @Index(name = "idx_inv_tx_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_inv_tx_type", columnList = "transaction_type"),
    @Index(name = "idx_inv_tx_date", columnList = "transaction_date"),
    @Index(name = "idx_inv_tx_ref", columnList = "reference_type, reference_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // GOODS_RECEIPT, SALES_ORDER, TRANSFER, ADJUSTMENT

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(length = 255)
    private String notes;

    // Transaction types
    public enum TransactionType {
        RECEIPT("Receipt", 1),           // Goods received
        ISSUE("Issue", -1),              // Goods issued
        TRANSFER_IN("Transfer In", 1),   // Transfer received
        TRANSFER_OUT("Transfer Out", -1), // Transfer sent
        ADJUSTMENT_PLUS("Adjustment +", 1),  // Stock adjustment increase
        ADJUSTMENT_MINUS("Adjustment -", -1), // Stock adjustment decrease
        RETURN_IN("Return In", 1),       // Customer return
        RETURN_OUT("Return Out", -1);    // Return to supplier

        private final String displayName;
        private final int multiplier;

        TransactionType(String displayName, int multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }

        public String getDisplayName() { return displayName; }
        public int getMultiplier() { return multiplier; }
    }

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (unitCost != null && quantity != null) {
            totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
