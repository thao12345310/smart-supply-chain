package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Entity
 * 
 * Represents a customer who places Sales Orders
 */
@Entity
@Table(name = "customer", indexes = {
    @Index(name = "idx_customer_code", columnList = "code"),
    @Index(name = "idx_customer_name", columnList = "name"),
    @Index(name = "idx_customer_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "payment_terms")
    private Integer paymentTerms;

    @Column
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SalesOrder> salesOrders = new ArrayList<>();

    // Helper methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (creditLimit == null) {
            creditLimit = BigDecimal.ZERO;
        }
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        if (paymentTerms == null) {
            paymentTerms = 30;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add delivery address
     */
    public void addDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.add(address);
        address.setCustomer(this);
    }

    /**
     * Remove delivery address
     */
    public void removeDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.remove(address);
        address.setCustomer(null);
    }

    /**
     * Get default delivery address
     */
    public DeliveryAddress getDefaultAddress() {
        return deliveryAddresses.stream()
            .filter(DeliveryAddress::getIsDefault)
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if customer has available credit
     */
    public boolean hasAvailableCredit(BigDecimal amount) {
        BigDecimal balance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
        BigDecimal limit = creditLimit != null ? creditLimit : BigDecimal.ZERO;
        return balance.add(amount).compareTo(limit) <= 0;
    }

    /**
     * Generate customer code
     */
    public static String generateCode() {
        return "CUST" + System.currentTimeMillis();
    }
}
