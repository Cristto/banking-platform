package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Transaction;

import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
