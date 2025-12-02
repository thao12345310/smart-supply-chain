package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "delivery_triproute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTripRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String shipperName;
    private String status; // Created, InProgress, Completed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_plan_id")
    private DeliveryPlan deliveryPlan;

    @OneToMany(mappedBy = "tripRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryTripRouteItem> items;
}
