package com.distribution.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryPlanShipperDTO {
    private Long id;
    private String shipperName;
    private String phone;
}
