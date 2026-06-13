package com.distribution.service;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountingService {

    /** Ghi 1 bút toán Nợ/Có. Gọi từ các service nghiệp vụ trong cùng transaction. */
    AccountingTransaction post(LocalDateTime when, String description,
                              String sourceType, Long sourceId,
                              AccountCode debit, AccountCode credit, BigDecimal amount);

    List<AccountingTransaction> getTransactions(LocalDateTime from, LocalDateTime to);

    /** Sổ cái 1 tài khoản kèm số dư lũy kế (Nợ +, Có -). */
    List<LedgerLine> getLedger(AccountCode account);

    record LedgerLine(AccountingTransaction tx, BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {}
}
