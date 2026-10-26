package com.banking.backend.domain.exception;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(Long sourceId, Long destinationId, String sourceCurrency, String destCurrency) {
        super("Accounts must use the same currency: source " + sourceId + " is " + sourceCurrency
                + ", destination " + destinationId + " is " + destCurrency);
    }
}
