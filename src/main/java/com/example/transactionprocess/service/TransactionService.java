
package com.example.transactionprocess.service;

import org.springframework.stereotype.Service;

import com.example.transactionprocess.model.TransactionStatus;
import com.example.transactionprocess.model.TransactionRequest;
import com.example.transactionprocess.processor.TransactionProcessor;
import com.example.transactionprocess.repository.TransactionStore;
import com.example.transactionprocess.dto.ProcessingSummary;

@Service
public class TransactionService {

    private final TransactionStore transactionStore;
    private final TransactionValidator validator;
    private final TransactionProcessor processor;
    private final RetryHandler retryHandler;

    public TransactionService(
            TransactionStore transactionStore,
            TransactionValidator validator,
            TransactionProcessor processor,
            RetryHandler retryHandler) {

        this.transactionStore = transactionStore;
        this.validator = validator;
        this.processor = processor;
        this.retryHandler = retryHandler;
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        return transactionStore.getTransactionStatus(transactionId);
    }
    
    public TransactionStatus getRequestStatus(String requestId) {
        return transactionStore.getRequestStatus(requestId);
    }
    
    public TransactionStatus processTransaction(TransactionRequest request) {

        // 1. Validate incoming request
        validator.validate(request);

        String transactionId = request.getTransactionId();
        String requestId = request.getRequestId();
        String accountId = request.getAccountId();

        // 2. Check whether this request was already received
        TransactionStatus previousRequestStatus =
                transactionStore.getRequestStatus(requestId);

        if (previousRequestStatus != null) {
            return previousRequestStatus;
        }

        // 3. Check whether this transaction was already processed
        TransactionStatus existingStatus =
                transactionStore.getTransactionStatus(transactionId);

        if (existingStatus != null) {

            if (existingStatus == TransactionStatus.PROCESSED) {

                transactionStore.saveRequestStatus(
                        requestId, TransactionStatus.DUPLICATE);

                return TransactionStatus.DUPLICATE;
            }

            if (existingStatus == TransactionStatus.PENDING ||
                    existingStatus == TransactionStatus.PROCESSING) {

                transactionStore.saveRequestStatus(
                        requestId, TransactionStatus.PENDING);

                return TransactionStatus.PENDING;
            }
        }

        // 4. Check expected sequence number
        int expectedSequence =
                transactionStore.getLastProcessedSequence(accountId) + 1;

        int receivedSequence = request.getSequenceNumber();

        // 5. Future sequence: store transaction for later processing
        if (receivedSequence > expectedSequence) {

            transactionStore.savePendingTransaction(accountId, request);

            transactionStore.saveTransactionStatus(
                    transactionId, TransactionStatus.PENDING);

            transactionStore.saveRequestStatus(
                    requestId, TransactionStatus.PENDING);

            return TransactionStatus.PENDING;
        }

        // 6. Old sequence: do not process again
        if (receivedSequence < expectedSequence) {

            transactionStore.saveRequestStatus(
                    requestId, TransactionStatus.DUPLICATE);

            return TransactionStatus.DUPLICATE;
        }

        // 7. Process expected transaction
        boolean processed = processOne(request);

        if (!processed) {
            return TransactionStatus.FAILED;
        }

        // 8. Process pending transactions in sequence
        processPendingTransactions(accountId);

        return TransactionStatus.PROCESSED;
    }

    private boolean processOne(TransactionRequest request) {

        String transactionId = request.getTransactionId();
        String requestId = request.getRequestId();

        transactionStore.saveTransactionStatus(
                transactionId, TransactionStatus.PROCESSING);

        transactionStore.saveRequestStatus(
                requestId, TransactionStatus.PROCESSING);

        // Retry transaction processing up to 3 attempts
        boolean processed = retryHandler.executeWithRetry(
                () -> processor.process(request)
        );

        if (processed) {

            transactionStore.saveTransactionStatus(
                    transactionId, TransactionStatus.PROCESSED);

            transactionStore.saveRequestStatus(
                    requestId, TransactionStatus.PROCESSED);

            transactionStore.updateLastProcessedSequence(
                    request.getAccountId(),
                    request.getSequenceNumber());

            return true;

        } else {

            transactionStore.saveTransactionStatus(
                    transactionId, TransactionStatus.FAILED);

            transactionStore.saveRequestStatus(
                    requestId, TransactionStatus.FAILED);

            return false;
        }
    }

    private void processPendingTransactions(String accountId) {

        while (true) {

            int nextSequence =
                    transactionStore.getLastProcessedSequence(accountId) + 1;

            TransactionRequest pendingRequest =
                    transactionStore.getPendingTransaction(
                            accountId, nextSequence);

            if (pendingRequest == null) {
                break;
            }

            boolean processed = processOne(pendingRequest);

            if (!processed) {
                break;
            }

            transactionStore.removePendingTransaction(
                    accountId, nextSequence);
        }
    }
    
    public ProcessingSummary getProcessingSummary() {

        var statuses = transactionStore.getAllRequestStatuses();

        int totalReceived = statuses.size();

        int processed = (int) statuses.stream()
                .filter(status -> status == TransactionStatus.PROCESSED)
                .count();

        int duplicates = (int) statuses.stream()
                .filter(status -> status == TransactionStatus.DUPLICATE)
                .count();

        int failed = (int) statuses.stream()
                .filter(status -> status == TransactionStatus.FAILED)
                .count();

        int pending = (int) statuses.stream()
                .filter(status -> status == TransactionStatus.PENDING
                        || status == TransactionStatus.RETRY_PENDING
                        || status == TransactionStatus.PROCESSING)
                .count();

        return new ProcessingSummary(
                totalReceived,
                processed,
                duplicates,
                failed,
                pending
        );
    }
    

    
    

public TransactionStatus processAcceptedTransaction(
        TransactionRequest request) {

    String accountId = request.getAccountId();

    // Get the lock specific to this account
    Object accountLock = transactionStore.getAccountLock(accountId);

    synchronized (accountLock) {

        // 1. Check expected sequence number
        int expectedSequence =
                transactionStore.getLastProcessedSequence(accountId) + 1;

        int receivedSequence = request.getSequenceNumber();

        // 2. Future sequence: keep it pending
        if (receivedSequence > expectedSequence) {

            transactionStore.savePendingTransaction(accountId, request);

            transactionStore.saveTransactionStatus(
                    request.getTransactionId(), TransactionStatus.PENDING);

            transactionStore.saveRequestStatus(
                    request.getRequestId(), TransactionStatus.PENDING);

            return TransactionStatus.PENDING;
        }

        // 3. Old sequence: do not process again
        if (receivedSequence < expectedSequence) {

            transactionStore.saveTransactionStatus(
                    request.getTransactionId(), TransactionStatus.DUPLICATE);

            transactionStore.saveRequestStatus(
                    request.getRequestId(), TransactionStatus.DUPLICATE);

            return TransactionStatus.DUPLICATE;
        }

        // 4. Process the expected transaction
        boolean processed = processOne(request);

        if (!processed) {
            return TransactionStatus.FAILED;
        }

        // 5. Process pending transactions in sequence
        processPendingTransactions(accountId);

        return TransactionStatus.PROCESSED;
    }
}


public boolean registerAcceptedRequest(TransactionRequest request) {

    // 1. Validate incoming request
    validator.validate(request);

    String transactionId = request.getTransactionId();
    String requestId = request.getRequestId();

    // 2. Atomically register both IDs
    boolean registered =
            transactionStore.registerRequestAndTransactionIfAbsent(
                    requestId,
                    transactionId
            );

    // 3. Reject duplicate request or transaction
    if (!registered) {
        return false;
    }

    return true;
}

}
