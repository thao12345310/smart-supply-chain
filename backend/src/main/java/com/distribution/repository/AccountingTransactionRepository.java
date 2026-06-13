package com.distribution.repository;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountingTransactionRepository extends JpaRepository<AccountingTransaction, Long> {

    List<AccountingTransaction> findByTxDateBetweenOrderByTxDateDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT t FROM AccountingTransaction t " +
           "WHERE t.debitAccount = :acc OR t.creditAccount = :acc ORDER BY t.txDate ASC")
    List<AccountingTransaction> findByAccount(@Param("acc") AccountCode acc);
}
