package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_order")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String status; // Pending, Shipped, Delivered
    private String destinationAddress;
}
