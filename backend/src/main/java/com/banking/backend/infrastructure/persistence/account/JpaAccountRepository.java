package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository springDataRepository;

    public JpaAccountRepository(SpringDataAccountRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Account save(Account account) {
        return springDataRepository.save(account);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        // Delegates to Spring Data — placeholder for future implementation
        // using a custom query on account_holders
        return List.of();
    }

    @Override
    public boolean existsByCustomerId(Long customerId) {
        // Delegates to Spring Data — placeholder for future implementation
        return false;
    }

    @Override
    public Account saveWithHolder(Account account, Customer customer, String role) {
        return springDataRepository.save(account);
    }
}
