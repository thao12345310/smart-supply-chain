package com.distribution.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    
    private Long id;
    
    private String code;
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    private String contactName;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;
    
    @DecimalMin(value = "0.00", message = "Credit limit must be positive")
    private BigDecimal creditLimit;
    
    private BigDecimal currentBalance;
    
    @Min(value = 0, message = "Payment terms must be positive")
    private Integer paymentTerms;
    
    private Boolean active;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related data
    private List<DeliveryAddressDTO> deliveryAddresses;
    
    // Computed field
    private BigDecimal availableCredit;
    
    /**
     * Calculate available credit
     */
    public void computeFields() {
        if (creditLimit != null && currentBalance != null) {
            this.availableCredit = creditLimit.subtract(currentBalance);
        }
    }
}
