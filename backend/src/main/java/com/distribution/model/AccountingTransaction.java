package com.distribution.model;

import com.distribution.model.enums.AccountCode;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bút toán đơn giản: mỗi bản ghi = 1 cặp Nợ/Có (debit/credit) cho 1 số tiền.
 * Sinh tự động từ nghiệp vụ, không cho sửa tay.
 */
@Entity
@Table(name = "accounting_transaction", indexes = {
    @Index(name = "idx_acctx_date", columnList = "tx_date"),
    @Index(name = "idx_acctx_source", columnList = "source_type, source_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tx_date", nullable = false)
    private LocalDateTime txDate;

    @Column(nullable = false, length = 255)
    private String description;

    /** GR | INVOICE | PAYMENT */
    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "debit_account", nullable = false, length = 20)
    private AccountCode debitAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_account", nullable = false, length = 20)
    private AccountCode creditAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
}
