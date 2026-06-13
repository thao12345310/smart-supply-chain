package com.distribution.model;

import com.distribution.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Phiếu thu/chi. RECEIPT gắn salesInvoiceId; DISBURSEMENT gắn purchaseOrderId. */
@Entity
@Table(name = "payment", indexes = {
    @Index(name = "idx_payment_type", columnList = "type"),
    @Index(name = "idx_payment_date", columnList = "payment_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(length = 50)
    private String method;

    @Column(name = "sales_invoice_id")
    private Long salesInvoiceId;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(length = 500)
    private String note;
}
