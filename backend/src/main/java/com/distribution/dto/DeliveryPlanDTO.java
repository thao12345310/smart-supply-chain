package com.distribution.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryPlanDTO {
    private Long id;
    private String code;
    private LocalDate createdDate;
    private String description;
    private String status;
    private List<Long> deliveryOrderIds;
    private List<DeliveryPlanShipperDTO> shippers;
}
