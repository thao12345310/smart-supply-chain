package com.distribution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestDTO {
    
    @NotBlank(message = "Action is required")
    private String action; // "APPROVE" or "REJECT"
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    private Long approvedBy;
    
    public boolean isApproval() {
        return "APPROVE".equalsIgnoreCase(action);
    }
    
    public boolean isRejection() {
        return "REJECT".equalsIgnoreCase(action);
    }
}
