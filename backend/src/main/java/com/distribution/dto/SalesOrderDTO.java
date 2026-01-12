package com.distribution.dto;

import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDTO {
    
    private Long id;
    
    private String code;
    
    @Size(max = 255, message = "Order name must not exceed 255 characters")
    private String orderName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDeliveryDate;
    
    @NotNull(message = "Customer is required")
    private Long customerId;
    private String customerName;
    private String customerCode;
    
    private Long deliveryAddressId;
    private String deliveryAddressText;
    
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    
    private SalesOrderStatus status;
    private String statusDisplayName;
    
    private PaymentStatus paymentStatus;
    private String paymentStatusDisplayName;
    
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    private BigDecimal discountAmount;
    
    @DecimalMin(value = "0.00", message = "Shipping cost must be positive")
    private BigDecimal shippingCost;
    
    private BigDecimal grandTotal;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime approvedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime completedDate;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private String rejectionReason;
    
    private Long createdBy;
    private String createdByName;
    
    private Long approvedBy;
    private String approvedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    @Valid
    private List<SalesOrderItemDTO> items;
    
    // Computed fields
    private Integer totalItems;
    private Integer totalQuantity;
    private Integer deliveredQuantity;
    private Double deliveredPercentage;
    private Integer remainingQuantity;
    
    // Related data
    private List<GoodsIssueDTO> goodsIssues;
    private List<SalesInvoiceDTO> invoices;
    
    /**
     * Create DTO with computed fields
     */
    public void computeFields() {
        if (items != null) {
            this.totalItems = items.size();
            this.totalQuantity = items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
            this.deliveredQuantity = items.stream()
                .mapToInt(item -> item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0)
                .sum();
            this.remainingQuantity = totalQuantity - deliveredQuantity;
            this.deliveredPercentage = totalQuantity > 0 
                ? (deliveredQuantity * 100.0 / totalQuantity) 
                : 0.0;
        }
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
        if (paymentStatus != null) {
            this.paymentStatusDisplayName = paymentStatus.getDisplayName();
        }
    }
}
