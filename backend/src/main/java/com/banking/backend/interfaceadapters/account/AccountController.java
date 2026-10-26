package com.banking.backend.interfaceadapters.account;

import com.banking.backend.application.account.GetAccount;
import com.banking.backend.application.account.GetAccountCommand;
import com.banking.backend.application.account.ListCustomerAccounts;
import com.banking.backend.application.account.ListCustomerAccountsCommand;
import com.banking.backend.application.account.OpenAccount;
import com.banking.backend.application.account.OpenAccountCommand;
import com.banking.backend.application.deposit.DepositFunds;
import com.banking.backend.application.deposit.DepositFundsCommand;
import com.banking.backend.application.transfer.TransferFunds;
import com.banking.backend.application.transfer.TransferFundsCommand;
import com.banking.backend.application.transaction.GetAccountTransactions;
import com.banking.backend.application.transaction.GetAccountTransactionsCommand;
import com.banking.backend.application.withdraw.WithdrawFunds;
import com.banking.backend.application.withdraw.WithdrawFundsCommand;
import com.banking.backend.domain.model.Account;
import com.banking.backend.interfaceadapters.account.AccountResponse;
import com.banking.backend.interfaceadapters.account.OpenAccountRequest;
import com.banking.backend.interfaceadapters.deposit.DepositRequest;
import com.banking.backend.interfaceadapters.deposit.DepositResponse;
import com.banking.backend.interfaceadapters.transaction.TransactionResponse;
import com.banking.backend.interfaceadapters.transfer.TransferRequest;
import com.banking.backend.interfaceadapters.transfer.TransferResponse;
import com.banking.backend.interfaceadapters.withdraw.WithdrawRequest;
import com.banking.backend.interfaceadapters.withdraw.WithdrawResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final OpenAccount openAccount;
    private final GetAccount getAccount;
    private final ListCustomerAccounts listAccounts;
    private final DepositFunds depositFunds;
    private final WithdrawFunds withdrawFunds;
    private final TransferFunds transferFunds;
    private final GetAccountTransactions getAccountTransactions;

    public AccountController(OpenAccount openAccount, GetAccount getAccount, ListCustomerAccounts listAccounts,
                             DepositFunds depositFunds, WithdrawFunds withdrawFunds, TransferFunds transferFunds,
                             GetAccountTransactions getAccountTransactions) {
        this.openAccount = openAccount;
        this.getAccount = getAccount;
        this.listAccounts = listAccounts;
        this.depositFunds = depositFunds;
        this.withdrawFunds = withdrawFunds;
        this.transferFunds = transferFunds;
        this.getAccountTransactions = getAccountTransactions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@RequestBody OpenAccountRequest request) {
        Account account = openAccount.execute(new OpenAccountCommand(request.customerId()));
        return AccountResponse.from(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        Account account = getAccount.execute(new GetAccountCommand(id));
        return AccountResponse.from(account);
    }

    @GetMapping("/customers/{customerId}")
    public List<AccountResponse> listCustomerAccounts(@PathVariable Long customerId) {
        List<Account> accounts = listAccounts.execute(new ListCustomerAccountsCommand(customerId));
        return accounts.stream().map(AccountResponse::from).toList();
    }

    @PostMapping("/{id}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse deposit(@PathVariable Long id, @RequestBody DepositRequest request) {
        return depositFunds.execute(new DepositFundsCommand(id, request.idempotencyKey(), request.amount()));
    }

    @PostMapping("/{id}/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public WithdrawResponse withdraw(@PathVariable Long id, @RequestBody WithdrawRequest request) {
        return withdrawFunds.execute(new WithdrawFundsCommand(id, request.idempotencyKey(), request.amount()));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return transferFunds.execute(new TransferFundsCommand(request.sourceAccountId(), request.destinationAccountId(),
                request.idempotencyKey(), request.amount()));
    }

    @GetMapping("/{id}/transactions")
    public Page<TransactionResponse> getAccountTransactions(@PathVariable Long id,
                                                            @PageableDefault Pageable pageable) {
        return getAccountTransactions.execute(new GetAccountTransactionsCommand(id, pageable));
    }
}
