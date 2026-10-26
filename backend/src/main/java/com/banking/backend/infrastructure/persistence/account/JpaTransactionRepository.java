package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.TransactionRepository;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaTransactionRepository implements TransactionRepository {

    private final SpringDataTransactionRepository springDataRepository;

    public JpaTransactionRepository(SpringDataTransactionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return springDataRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Page<Transaction> findBySourceAccountIdOrDestinationAccountId(Long sourceAccountId, Long destinationAccountId, org.springframework.data.domain.Pageable pageable) {
        return springDataRepository.findBySourceAccountIdOrDestinationAccountId(sourceAccountId, destinationAccountId, pageable);
    }
}
