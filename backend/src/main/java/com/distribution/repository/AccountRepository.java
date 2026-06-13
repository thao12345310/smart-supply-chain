package com.distribution.repository;

import com.distribution.model.Account;
import com.distribution.model.enums.AccountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(AccountCode code);
}
