package com.example.transactionprocess.repository;

import java.util.Collection;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.transactionprocess.model.TransactionRequest;
import com.example.transactionprocess.model.TransactionStatus;

@Component
	public class TransactionStore {

	    // Stores the status of each unique business transaction
	    private final Map<String, TransactionStatus> transactionStatuses = new ConcurrentHashMap<>();
	    
		 // Stores transactions waiting for their sequence number
	    private final Map<String, TransactionRequest> pendingTransactions = new ConcurrentHashMap<>();
	    
	    public Collection<TransactionStatus> getAllRequestStatuses() {
	        return requestStatuses.values();
	    }

	    // Stores the status of each incoming request
	    private final Map<String, TransactionStatus> requestStatuses =
	            new ConcurrentHashMap<>();

	    // Stores the balance of each account
	    private final Map<String, BigDecimal> accountBalances =
	            new ConcurrentHashMap<>();

	    // Stores the last processed sequence number for each account
	    private final Map<String, Integer> lastProcessedSequence =
	            new ConcurrentHashMap<>();

	    public TransactionStatus getTransactionStatus(String transactionId) {
	        return transactionStatuses.get(transactionId);
	    }

	    public void saveTransactionStatus(
	            String transactionId, TransactionStatus status) {
	        transactionStatuses.put(transactionId, status);
	    }

	    public void saveRequestStatus(String requestId, TransactionStatus status) {
	        requestStatuses.put(requestId, status);
	    }

	    public TransactionStatus getRequestStatus(String requestId) {
	        return requestStatuses.get(requestId);
	    }

	    public BigDecimal getBalance(String accountId) {
	        return accountBalances.getOrDefault(accountId, BigDecimal.ZERO);
	    }

	    public void updateBalance(String accountId, BigDecimal balance) {
	        accountBalances.put(accountId, balance);
	    }

	    public Integer getLastProcessedSequence(String accountId) {
	        return lastProcessedSequence.getOrDefault(accountId, 0);
	    }

	    public void updateLastProcessedSequence(
	            String accountId, Integer sequenceNumber) {
	        lastProcessedSequence.put(accountId, sequenceNumber);
	    }
	    

	    public void savePendingTransaction( String accountId, TransactionRequest request) {

	        String key = accountId + ":" + request.getSequenceNumber();

	        pendingTransactions.put(key, request);
	    }

	    public TransactionRequest getPendingTransaction(String accountId, Integer sequenceNumber) {

	        String key = accountId + ":" + sequenceNumber;

	        return pendingTransactions.get(key);
	    }

	    public void removePendingTransaction(String accountId, Integer sequenceNumber) {

	        String key = accountId + ":" + sequenceNumber;

	        pendingTransactions.remove(key);
	    }
	    
	    public boolean registerRequestIfAbsent(String requestId) {
	        return requestStatuses.putIfAbsent(
	                requestId,
	                TransactionStatus.RECEIVED
	        ) == null;
	    }

	    public boolean registerTransactionIfAbsent(String transactionId) {
	        return transactionStatuses.putIfAbsent(
	                transactionId,
	                TransactionStatus.RECEIVED
	        ) == null;
	    }
	    
	    private final Map<String, Object> accountLocks =
	            new ConcurrentHashMap<>();
	    
public synchronized boolean registerRequestAndTransactionIfAbsent(
        String requestId,
        String transactionId) {

    // Check both IDs before registering anything
    if (requestStatuses.containsKey(requestId)
            || transactionStatuses.containsKey(transactionId)) {
        return false;
    }

    // Register both IDs together
    requestStatuses.put(requestId, TransactionStatus.RECEIVED);

    transactionStatuses.put(transactionId, TransactionStatus.RECEIVED);

    return true;
}

public Object getAccountLock(String accountId) {

    return accountLocks.computeIfAbsent(
            accountId,
            id -> new Object()
    );
}
}
