package com.banking.backend.infrastructure.persistence.account;

import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.AccountHolder;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository springDataRepository;
    private final SpringDataAccountHolderRepository accountHolderRepository;

    public JpaAccountRepository(SpringDataAccountRepository springDataRepository,
                                SpringDataAccountHolderRepository accountHolderRepository) {
        this.springDataRepository = springDataRepository;
        this.accountHolderRepository = accountHolderRepository;
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
        List<AccountHolder> holders = accountHolderRepository.findByCustomerId(customerId);

        if (holders.isEmpty()) {
            return List.of();
        }

        List<Long> accountIds = holders.stream()
                .map(holder -> holder.getAccount().getId())
                .distinct()
                .toList();

        return springDataRepository.findAllById(accountIds);
    }

    @Override
    public boolean existsByCustomerId(Long customerId) {
        return accountHolderRepository.existsByCustomerId(customerId);
    }

    @Override
    public Account saveWithHolder(Account account, Customer customer, String role) {
        return springDataRepository.save(account);
    }
}
