package com.banking.backend.interfaceadapters.account;

import com.banking.backend.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        String status,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        List<Long> holderCustomerIds) {

    public static AccountResponse from(Account account) {
        List<Long> holderCustomerIds = account.getAccountHolders().stream()
                .map(holder -> holder.getCustomer().getId())
                .toList();

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt(),
                account.getClosedAt(),
                holderCustomerIds
        );
    }
}
