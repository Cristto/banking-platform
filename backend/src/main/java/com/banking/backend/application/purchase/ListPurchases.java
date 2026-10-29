package com.banking.backend.application.purchase;

import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import com.banking.backend.interfaceadapters.purchase.PurchaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPurchases {

    private final AccountRepository accounts;
    private final PurchaseRepository purchases;

    public ListPurchases(AccountRepository accounts, PurchaseRepository purchases) {
        this.accounts = accounts;
        this.purchases = purchases;
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> execute(ListPurchasesCommand command) {
        Account account = accounts.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        return purchases.findByAccountIdOrderByCreatedAtDesc(
                        command.accountId(), command.pageable())
                .map(purchase -> PurchaseResponse.from(purchase, account.getAvailableFunds()));
    }
}
