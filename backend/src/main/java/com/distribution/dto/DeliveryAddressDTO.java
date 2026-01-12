package com.distribution.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAddressDTO {
    
    private Long id;
    
    private Long customerId;
    private String customerName;
    
    @Size(max = 255, message = "Address name must not exceed 255 characters")
    private String addressName;
    
    @Size(max = 255, message = "Recipient name must not exceed 255 characters")
    private String recipientName;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;
    
    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    private Boolean isDefault;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    // Computed field
    private String fullAddress;
    
    /**
     * Build full address string
     */
    public void computeFields() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) {
            sb.append(", ").append(addressLine2);
        }
        if (city != null && !city.isBlank()) {
            sb.append(", ").append(city);
        }
        if (state != null && !state.isBlank()) {
            sb.append(", ").append(state);
        }
        if (postalCode != null && !postalCode.isBlank()) {
            sb.append(" ").append(postalCode);
        }
        if (country != null && !country.isBlank()) {
            sb.append(", ").append(country);
        }
        this.fullAddress = sb.toString();
    }
}
