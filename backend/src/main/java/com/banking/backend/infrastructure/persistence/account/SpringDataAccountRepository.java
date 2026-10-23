package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findById(Long id);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
