package com.example.transactionprocess.processor;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.transactionprocess.model.TransactionRequest;
import com.example.transactionprocess.repository.TransactionStore;

@Component
public class TransactionProcessor {

    private final TransactionStore transactionStore;

    public TransactionProcessor(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    public void process(TransactionRequest request) {

        String accountId = request.getAccountId();

        BigDecimal currentBalance = transactionStore.getBalance(accountId);

        BigDecimal amount = request.getAmount();

        BigDecimal updatedBalance;

        switch (request.getTransactionType()) {

            case CREDIT:
                updatedBalance = currentBalance.add(amount);
                break;

            case DEBIT:
                if (currentBalance.compareTo(amount) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient account balance");
                }

                updatedBalance = currentBalance.subtract(amount);
                break;

            case REVERSAL:
                // For this initial version, reversal adds the amount back.
                updatedBalance = currentBalance.add(amount);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported transaction type");
        }

        transactionStore.updateBalance(accountId, updatedBalance);

        System.out.println("Transaction processed: "
                + request.getTransactionId());

        System.out.println("Updated balance: " + updatedBalance);
    }
    
    
}
