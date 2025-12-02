package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_plan_shipper")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryPlanShipper {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shipperName;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "delivery_plan_id")
    private DeliveryPlan deliveryPlan;
}
