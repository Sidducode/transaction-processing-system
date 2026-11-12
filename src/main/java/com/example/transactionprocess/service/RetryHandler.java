package com.example.transactionprocess.service;

import org.springframework.stereotype.Component;

@Component
public class RetryHandler {

    private static final int MAX_ATTEMPTS = 3;

    public boolean executeWithRetry(Runnable operation) {

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {
                operation.run();

                System.out.println(
                        "Transaction successful on attempt: " + attempt);

                return true;

            } catch (RuntimeException exception) {

                System.out.println(
                        "Attempt " + attempt + " failed: "
                                + exception.getMessage());

                if (attempt == MAX_ATTEMPTS) {
                    System.out.println(
                            "Maximum retry attempts reached.");
                    return false;
                }
            }
        }

        return false;
    }
}
