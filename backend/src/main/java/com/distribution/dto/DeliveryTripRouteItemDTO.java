package com.distribution.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryTripRouteItemDTO {
    private Long id;
    private Integer sequenceNo;
    private String status;
    private Long deliveryOrderId;
    private String deliveryOrderCode;
}
