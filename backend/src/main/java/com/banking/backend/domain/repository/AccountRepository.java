package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
