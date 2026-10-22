package com.banking.backend.interfaceadapters.account;

import com.banking.backend.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AccountResponse(
        Long id,
        String accountNumber,
        Long holderCustomerId,
        BigDecimal balance,
        String currency,
        String status,
        LocalDateTime createdAt,
        LocalDateTime closedAt) {

    public static AccountResponse from(Account account) {
        Long holderCustomerId = account.getAccountHolders().isEmpty()
                ? null
                : account.getAccountHolders().get(0).getCustomer().getId();

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                holderCustomerId,
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt(),
                account.getClosedAt()
        );
    }
}
