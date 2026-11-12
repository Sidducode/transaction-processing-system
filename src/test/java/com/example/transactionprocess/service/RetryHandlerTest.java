package com.example.transactionprocess.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryHandlerTest {

    @Test
    void shouldRetryAndSucceedOnSecondAttempt() {

        RetryHandler retryHandler = new RetryHandler();

        AtomicInteger attempts = new AtomicInteger(0);

        boolean result = retryHandler.executeWithRetry(() -> {

            int attempt = attempts.incrementAndGet();

            if (attempt == 1) {
                throw new RuntimeException("Temporary failure");
            }

            System.out.println("Processing successful");
        });

        assertTrue(result);
        assertEquals(2, attempts.get());
    }
    
    @Test
    void shouldFailAfterMaximumAttempts() {

        RetryHandler retryHandler = new RetryHandler();

        AtomicInteger attempts = new AtomicInteger(0);

        boolean result = retryHandler.executeWithRetry(() -> {

            attempts.incrementAndGet();

            throw new RuntimeException("Service unavailable");
        });

        assertFalse(result);
        assertEquals(3, attempts.get());
    }
}
