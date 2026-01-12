package com.distribution.dto;

import com.distribution.model.enums.PurchaseOrderStatus;
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
public class PurchaseOrderDTO {
    
    private Long id;
    
    private String code;
    
    @Size(max = 255, message = "Order name must not exceed 255 characters")
    private String orderName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deliveryDate;
    
    @NotNull(message = "Supplier is required")
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    
    private PurchaseOrderStatus status;
    private String statusDisplayName;
    
    private String taxType;
    
    @DecimalMin(value = "0.00", message = "Shipping cost must be positive")
    private BigDecimal shippingCost;
    
    private BigDecimal totalAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;
    
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
    
    @Valid
    private List<PurchaseOrderItemDTO> items;
    
    // Computed fields
    private Integer totalItems;
    private Integer totalQuantity;
    private Integer receivedQuantity;
    private Double receivedPercentage;
    
    /**
     * Create DTO with computed fields
     */
    public void computeFields() {
        if (items != null) {
            this.totalItems = items.size();
            this.totalQuantity = items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
            this.receivedQuantity = items.stream()
                .mapToInt(item -> item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0)
                .sum();
            this.receivedPercentage = totalQuantity > 0 
                ? (receivedQuantity * 100.0 / totalQuantity) 
                : 0.0;
        }
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
    }
}
