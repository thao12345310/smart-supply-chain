package com.distribution.service;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.repository.AccountingTransactionRepository;
import com.distribution.service.impl.AccountingServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountingServiceTest {

    private final AccountingTransactionRepository repo = mock(AccountingTransactionRepository.class);
    private final AccountingServiceImpl service = new AccountingServiceImpl(repo);

    @Test
    void ledger_computes_running_balance_for_AR() {
        AccountingTransaction invoice = AccountingTransaction.builder()
            .txDate(LocalDateTime.now().minusDays(1)).description("HĐ")
            .debitAccount(AccountCode.AR).creditAccount(AccountCode.REVENUE)
            .amount(new BigDecimal("100")).build();
        AccountingTransaction receipt = AccountingTransaction.builder()
            .txDate(LocalDateTime.now()).description("Thu")
            .debitAccount(AccountCode.CASH).creditAccount(AccountCode.AR)
            .amount(new BigDecimal("30")).build();
        when(repo.findByAccount(AccountCode.AR)).thenReturn(List.of(invoice, receipt));

        List<AccountingService.LedgerLine> lines = service.getLedger(AccountCode.AR);

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).runningBalance()).isEqualByComparingTo("100");
        assertThat(lines.get(1).runningBalance()).isEqualByComparingTo("70");
    }

    @Test
    void post_skips_non_positive_amount() {
        AccountingTransaction tx = service.post(LocalDateTime.now(), "x", "GR", 1L,
            AccountCode.INVENTORY, AccountCode.AP, BigDecimal.ZERO);
        assertThat(tx).isNull();
        verify(repo, never()).save(any());
    }
}
