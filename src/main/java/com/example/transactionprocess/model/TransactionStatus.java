package com.example.transactionprocess.model;

public enum TransactionStatus {
	
	RECEIVED,
    PROCESSING,
    PROCESSED,
    DUPLICATE,
    FAILED,
    RETRY_PENDING,
    PENDING

}
