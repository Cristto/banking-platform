package com.banking.backend.interfaceadapters.account;

import com.banking.backend.application.account.OpenAccount;
import com.banking.backend.application.account.OpenAccountCommand;
import com.banking.backend.domain.model.Account;
import com.banking.backend.interfaceadapters.account.OpenAccountRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final OpenAccount openAccount;

    public AccountController(OpenAccount openAccount) {
        this.openAccount = openAccount;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@RequestBody OpenAccountRequest request) {
        Account account = openAccount.execute(new OpenAccountCommand(request.customerId(), request.role()));
        return AccountResponse.from(account);
    }
}
