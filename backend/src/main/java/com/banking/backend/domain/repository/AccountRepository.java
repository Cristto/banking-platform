package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Account;


import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByIdForUpdate(Long id);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
