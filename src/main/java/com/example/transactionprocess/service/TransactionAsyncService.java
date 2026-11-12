package com.example.transactionprocess.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.transactionprocess.model.TransactionRequest;

@Service
public class TransactionAsyncService {

    private final TransactionService transactionService;

    public TransactionAsyncService(
            TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Async("transactionTaskExecutor")
    public void processInBackground(TransactionRequest request) {

        System.out.println("Background processing started: "
                + request.getTransactionId()
                + " | Thread: " + Thread.currentThread().getName());

        try {
            transactionService.processAcceptedTransaction(request);

            System.out.println("Background processing completed: "
                    + request.getTransactionId());

        } catch (Exception exception) {

            System.out.println("Background processing failed: "
                    + request.getTransactionId()
                    + " | Reason: " + exception.getMessage());
        }
    }
}

