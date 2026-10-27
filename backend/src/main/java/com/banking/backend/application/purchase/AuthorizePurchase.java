package com.banking.backend.application.purchase;

import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import com.banking.backend.interfaceadapters.purchase.PurchaseResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthorizePurchase {

    private final AccountRepository accounts;
    private final PurchaseRepository purchases;

    public AuthorizePurchase(AccountRepository accounts, PurchaseRepository purchases) {
        this.accounts = accounts;
        this.purchases = purchases;
    }

    @Transactional
    public PurchaseResponse execute(AuthorizePurchaseCommand command) {
        Account account = accounts.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Optional<Purchase> existingOpt = purchases.findByPurchaseReference(command.purchaseReference());
        if (existingOpt.isPresent()) {
            Purchase existing = existingOpt.get();
            return PurchaseResponse.from(existing, getAvailableBalance(account));
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getId());
        }

        checkSufficientAvailableFunds(account, command.amount());

        Purchase purchase = createPurchase(command, account);
        reserveFunds(account, command.amount());

        purchase = purchases.save(purchase);
        return PurchaseResponse.from(purchase, getAvailableBalance(account));
    }

    private void checkSufficientAvailableFunds(Account account, BigDecimal amount) {
        BigDecimal available = account.getBalance().subtract(account.getReservedBalance());
        if (available.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    account.getId(), amount, available);
        }
    }

    private Purchase createPurchase(AuthorizePurchaseCommand command, Account account) {
        Purchase purchase = new Purchase(
                account,
                command.purchaseReference(),
                command.merchantName(),
                command.amount(),
                PurchaseStatus.AUTHORIZED
        );
        purchase.setAuthorizedAt(LocalDateTime.now());
        return purchase;
    }

    private void reserveFunds(Account account, BigDecimal amount) {
        BigDecimal newReserved = account.getReservedBalance().add(amount);
        account.setReservedBalance(newReserved);
        accounts.save(account);
    }

    private BigDecimal getAvailableBalance(Account account) {
        return account.getBalance().subtract(account.getReservedBalance());
    }
}
