package com.distribution.dto;

import com.distribution.model.enums.GoodsIssueStatus;
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
public class GoodsIssueDTO {
    
    private Long id;
    
    private String code;
    
    @NotNull(message = "Sales Order is required")
    private Long salesOrderId;
    private String salesOrderCode;
    
    private GoodsIssueStatus status;
    private String statusDisplayName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime confirmedDate;
    
    @Size(max = 100, message = "Delivery note number must not exceed 100 characters")
    private String deliveryNoteNumber;
    
    private BigDecimal totalAmount;
    
    @Size(max = 100, message = "Shipping method must not exceed 100 characters")
    private String shippingMethod;
    
    @Size(max = 100, message = "Tracking number must not exceed 100 characters")
    private String trackingNumber;
    
    @Size(max = 100, message = "Carrier name must not exceed 100 characters")
    private String carrierName;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    
    private Long deliveryAddressId;
    private String deliveryAddressText;
    
    // Audit fields
    private Long createdBy;
    private String createdByName;
    
    private Long confirmedBy;
    private String confirmedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    // Customer info (from SO)
    private Long customerId;
    private String customerName;
    
    @Valid
    private List<GoodsIssueItemDTO> items;
    
    // Related data
    private SalesInvoiceDTO invoice;
    
    // Computed fields
    private Integer totalItems;
    private Integer totalIssuedQuantity;
    
    /**
     * Compute fields
     */
    public void computeFields() {
        if (items != null) {
            this.totalItems = items.size();
            this.totalIssuedQuantity = items.stream()
                .mapToInt(item -> item.getIssuedQuantity() != null ? item.getIssuedQuantity() : 0)
                .sum();
        }
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
    }
}
