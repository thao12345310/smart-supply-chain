package com.distribution.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryOrderDTO {
    private Long id;
    private String code;
    private String status;
    private String destinationAddress;
}
