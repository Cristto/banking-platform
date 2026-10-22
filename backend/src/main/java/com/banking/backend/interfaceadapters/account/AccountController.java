package com.banking.backend.interfaceadapters.account;

import com.banking.backend.application.account.GetAccount;
import com.banking.backend.application.account.GetAccountCommand;
import com.banking.backend.application.account.OpenAccount;
import com.banking.backend.application.account.OpenAccountCommand;
import com.banking.backend.domain.model.Account;
import com.banking.backend.interfaceadapters.account.OpenAccountRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final OpenAccount openAccount;
    private final GetAccount getAccount;

    public AccountController(OpenAccount openAccount, GetAccount getAccount) {
        this.openAccount = openAccount;
        this.getAccount = getAccount;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@RequestBody OpenAccountRequest request) {
        Account account = openAccount.execute(new OpenAccountCommand(request.customerId(), request.role()));
        return AccountResponse.from(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        Account account = getAccount.execute(new GetAccountCommand(id));
        return AccountResponse.from(account);
    }
}
