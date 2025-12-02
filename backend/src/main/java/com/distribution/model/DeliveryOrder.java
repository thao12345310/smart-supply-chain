package com.distribution.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String status; // Pending, Shipped, Delivered
    private String destinationAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private PurchaseOrder salesOrder; // reuse from PO or separate SalesOrder if available
}
