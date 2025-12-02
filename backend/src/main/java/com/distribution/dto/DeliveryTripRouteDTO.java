package com.distribution.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryTripRouteDTO {
    private Long id;
    private String code;
    private String shipperName;
    private String status;
    private Long deliveryPlanId;
    private List<DeliveryTripRouteItemDTO> items;
}
