package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_triproute_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryTripRouteItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sequenceNo;
    private String status; // Pending, Delivered, Failed

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "triproute_id")
    private DeliveryTripRoute tripRoute;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "delivery_order_id")
    private DeliveryOrder deliveryOrder;
}
