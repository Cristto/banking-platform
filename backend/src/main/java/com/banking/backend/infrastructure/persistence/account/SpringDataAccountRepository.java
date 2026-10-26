package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
