package com.example.transactionprocess.service;

import com.example.transactionprocess.model.TransactionRequest;
import com.example.transactionprocess.model.TransactionStatus;
import com.example.transactionprocess.model.TransactionType;
import com.example.transactionprocess.processor.TransactionProcessor;
import com.example.transactionprocess.repository.TransactionStore;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionStore store = new TransactionStore();

    private TransactionService service = new TransactionService(
            store,
            new TransactionValidator(),
            new TransactionProcessor(store),
            new RetryHandler()
    );

    private TransactionRequest createRequest(
            String transactionId,
            String requestId,
            String accountId,
            TransactionType type,
            String amount,
            int sequence) {

        TransactionRequest request = new TransactionRequest();

        request.setTransactionId(transactionId);
        request.setRequestId(requestId);
        request.setAccountId(accountId);
        request.setTransactionType(type);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("INR");
        request.setSequenceNumber(sequence);

        return request;
    }

    @Test
    void shouldDetectDuplicateTransaction() {

        TransactionRequest first = createRequest(
                "TXN-101", "REQ-101", "ACC-101",
                TransactionType.CREDIT, "100", 1
        );

        TransactionRequest duplicate = createRequest(
                "TXN-101", "REQ-102", "ACC-101",
                TransactionType.CREDIT, "100", 1
        );

        assertEquals(
                TransactionStatus.PROCESSED,
                service.processTransaction(first)
        );

        assertEquals(
                TransactionStatus.DUPLICATE,
                service.processTransaction(duplicate)
        );

        assertEquals(
                new BigDecimal("100"),
                store.getBalance("ACC-101")
        );
    }

    @Test
    void shouldProcessOutOfOrderTransactions() {

        TransactionRequest second = createRequest(
                "TXN-202", "REQ-202", "ACC-202",
                TransactionType.DEBIT, "40", 2
        );

        TransactionRequest first = createRequest(
                "TXN-201", "REQ-201", "ACC-202",
                TransactionType.CREDIT, "100", 1
        );

        assertEquals(
                TransactionStatus.PENDING,
                service.processTransaction(second)
        );

        assertEquals(
                TransactionStatus.PROCESSED,
                service.processTransaction(first)
        );

        assertEquals(
                TransactionStatus.PROCESSED,
                store.getRequestStatus("REQ-202")
        );

        assertEquals(
                new BigDecimal("60"),
                store.getBalance("ACC-202")
        );
    }
    
    @Test
    void shouldRejectNegativeTransactionAmount() {

        TransactionRequest request = createRequest(
                "TXN-301",
                "REQ-301",
                "ACC-301",
                TransactionType.CREDIT,
                "-10",
                1
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.processTransaction(request)
        );

        assertEquals(
                BigDecimal.ZERO,
                store.getBalance("ACC-301")
        );
    }
}

