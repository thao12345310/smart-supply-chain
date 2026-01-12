package com.distribution.dto;

import com.distribution.model.DeliveryTripRoute.TripStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Delivery Trip Route
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTripRouteDTO {
    
    private Long id;
    private String code;
    
    // Shipper information
    private Long shipperUserId;
    private String shipperName;
    private String shipperUsername;
    
    // Status
    private TripStatus status;
    private String statusDisplayName;
    
    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime completedAt;
    
    // Notes
    private String notes;
    
    // Delivery Plan reference
    private Long deliveryPlanId;
    private String deliveryPlanDescription;
    
    // Trip items
    private List<DeliveryTripRouteItemDTO> items;
    
    // Computed fields
    private Integer totalItems;
    private Integer completedItems;
    private Double completionPercentage;
    
    /**
     * Compute display fields
     */
    public void computeFields() {
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
        if (items != null) {
            this.totalItems = items.size();
            // completedItems can be calculated based on item status
        }
    }
    
    /**
     * Inner DTO for trip items
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryTripRouteItemDTO {
        private Long id;
        private Long deliveryOrderId;
        private String deliveryOrderCode;
        private String customerName;
        private String deliveryAddress;
        private Integer sequence;
        private String status;
        private String notes;
    }
}
