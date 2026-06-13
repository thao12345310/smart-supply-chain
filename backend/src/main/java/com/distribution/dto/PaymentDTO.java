package com.distribution.dto;

import com.distribution.model.enums.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDTO {
    private Long id;
    private String code;
    private PaymentType type;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private Long salesInvoiceId;
    private Long purchaseOrderId;
    private String note;
}
