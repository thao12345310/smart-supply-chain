package com.distribution.dto;

import com.distribution.model.enums.GoodsReceiptStatus;
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
public class GoodsReceiptDTO {
    
    private Long id;
    
    private String code;
    
    @NotNull(message = "Purchase Order is required")
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    
    private Long warehouseId;
    private String warehouseName;
    
    private GoodsReceiptStatus status;
    private String statusDisplayName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiptDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime confirmedDate;
    
    @Size(max = 100, message = "Delivery note number must not exceed 100 characters")
    private String deliveryNoteNumber;
    
    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    private String invoiceNumber;
    
    private BigDecimal totalAmount;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private Long createdBy;
    private String createdByName;
    
    private Long confirmedBy;
    private String confirmedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    @Valid
    private List<GoodsReceiptItemDTO> items;
    
    // Computed fields
    private Integer totalItems;
    private Integer totalReceivedQuantity;
    private Integer totalAcceptedQuantity;
    private Integer totalRejectedQuantity;
    
    /**
     * Compute summary fields
     */
    public void computeFields() {
        if (items != null) {
            this.totalItems = items.size();
            this.totalReceivedQuantity = items.stream()
                .mapToInt(item -> item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0)
                .sum();
            this.totalAcceptedQuantity = items.stream()
                .mapToInt(item -> item.getAcceptedQuantity() != null ? item.getAcceptedQuantity() : 0)
                .sum();
            this.totalRejectedQuantity = items.stream()
                .mapToInt(item -> item.getRejectedQuantity() != null ? item.getRejectedQuantity() : 0)
                .sum();
        }
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
    }
}
