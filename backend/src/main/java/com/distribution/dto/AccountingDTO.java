package com.distribution.dto;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.service.AccountingService;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountingDTO {

    @Getter @Setter @Builder
    public static class TransactionRow {
        private Long id;
        private LocalDateTime txDate;
        private String description;
        private String sourceType;
        private Long sourceId;
        private AccountCode debitAccount;
        private AccountCode creditAccount;
        private BigDecimal amount;

        public static TransactionRow of(AccountingTransaction t) {
            return TransactionRow.builder()
                .id(t.getId()).txDate(t.getTxDate()).description(t.getDescription())
                .sourceType(t.getSourceType()).sourceId(t.getSourceId())
                .debitAccount(t.getDebitAccount()).creditAccount(t.getCreditAccount())
                .amount(t.getAmount()).build();
        }
    }

    @Getter @Setter @Builder
    public static class LedgerRow {
        private LocalDateTime txDate;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal runningBalance;

        public static LedgerRow of(AccountingService.LedgerLine l) {
            return LedgerRow.builder()
                .txDate(l.tx().getTxDate()).description(l.tx().getDescription())
                .debit(l.debit()).credit(l.credit()).runningBalance(l.runningBalance()).build();
        }
    }
}
