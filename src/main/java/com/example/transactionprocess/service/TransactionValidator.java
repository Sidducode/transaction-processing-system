package com.example.transactionprocess.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.transactionprocess.model.TransactionRequest;

@Component
public class TransactionValidator {

    public void validate(TransactionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }

        if (isBlank(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

        if (isBlank(request.getRequestId())) {
            throw new IllegalArgumentException("Request ID is required");
        }

        if (isBlank(request.getAccountId())) {
            throw new IllegalArgumentException("Account ID is required");
        }

        if (request.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (isBlank(request.getCurrency())) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (request.getSequenceNumber() == null ||
                request.getSequenceNumber() <= 0) {
            throw new IllegalArgumentException("Sequence number must be greater than zero");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
