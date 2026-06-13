package com.distribution.service.impl;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.repository.AccountingTransactionRepository;
import com.distribution.service.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountingServiceImpl implements AccountingService {

    private final AccountingTransactionRepository txRepo;

    @Override
    public AccountingTransaction post(LocalDateTime when, String description,
                                      String sourceType, Long sourceId,
                                      AccountCode debit, AccountCode credit, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Bỏ qua bút toán {} vì amount không hợp lệ: {}", description, amount);
            return null;
        }
        AccountingTransaction tx = AccountingTransaction.builder()
            .txDate(when != null ? when : LocalDateTime.now())
            .description(description)
            .sourceType(sourceType)
            .sourceId(sourceId)
            .debitAccount(debit)
            .creditAccount(credit)
            .amount(amount)
            .build();
        tx = txRepo.save(tx);
        log.info("Bút toán #{}: Nợ {} / Có {} = {} ({})", tx.getId(), debit, credit, amount, description);
        return tx;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingTransaction> getTransactions(LocalDateTime from, LocalDateTime to) {
        return txRepo.findByTxDateBetweenOrderByTxDateDesc(from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerLine> getLedger(AccountCode account) {
        List<AccountingTransaction> txs = txRepo.findByAccount(account);
        List<LedgerLine> lines = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;
        for (AccountingTransaction tx : txs) {
            BigDecimal debit = tx.getDebitAccount() == account ? tx.getAmount() : BigDecimal.ZERO;
            BigDecimal credit = tx.getCreditAccount() == account ? tx.getAmount() : BigDecimal.ZERO;
            balance = balance.add(debit).subtract(credit);
            lines.add(new LedgerLine(tx, debit, credit, balance));
        }
        return lines;
    }
}
