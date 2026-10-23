package com.banking.backend.interfaceadapters.account;

import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.CustomerInactiveException;
import com.banking.backend.domain.exception.CustomerNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AccountController.class)
public class AccountExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handle(CustomerNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handle(AccountNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CustomerInactiveException.class)
    public ProblemDetail handle(CustomerInactiveException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handle(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
