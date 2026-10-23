package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.AccountHolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountHolderRepository extends JpaRepository<AccountHolder, Long> {

    List<AccountHolder> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);

    Optional<AccountHolder> findByAccountIdAndCustomerId(Long accountId, Long customerId);
}
