package com.distribution.dto;

import com.distribution.model.enums.SalesInvoiceStatus;
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
public class SalesInvoiceDTO {
    
    private Long id;
    
    private String code;
    
    private Long salesOrderId;
    private String salesOrderCode;
    
    private Long goodsIssueId;
    private String goodsIssueCode;
    
    private Long customerId;
    private String customerName;
    private String customerCode;
    private String customerEmail;
    
    private SalesInvoiceStatus status;
    private String statusDisplayName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime issuedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime paidDate;
    
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    private BigDecimal discountAmount;
    
    @DecimalMin(value = "0.00", message = "Shipping cost must be positive")
    private BigDecimal shippingCost;
    
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
    
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    // Audit fields
    private Long createdBy;
    private String createdByName;
    
    private Long issuedBy;
    private String issuedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    
    @Valid
    private List<SalesInvoiceItemDTO> items;
    
    // Computed fields
    private Integer totalItems;
    private Boolean overdue;
    private Integer daysUntilDue;
    private Integer daysOverdue;
    
    /**
     * Compute fields
     */
    public void computeFields() {
        if (items != null) {
            this.totalItems = items.size();
        }
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
        if (dueDate != null) {
            LocalDate today = LocalDate.now();
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
            if (daysDiff >= 0) {
                this.daysUntilDue = (int) daysDiff;
                this.daysOverdue = 0;
                this.overdue = false;
            } else {
                this.daysUntilDue = 0;
                this.daysOverdue = (int) Math.abs(daysDiff);
                this.overdue = (status == SalesInvoiceStatus.ISSUED || 
                               status == SalesInvoiceStatus.PARTIALLY_PAID);
            }
        }
    }
}
