package com.example.transactionprocess.model;

import java.math.BigDecimal;

public class TransactionRequest {
	
	    private String transactionId;

	    private String requestId;

	    private String accountId;

	    private TransactionType transactionType;

	    private BigDecimal amount;

	    private String currency;

	    private Integer sequenceNumber;

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}

		public String getAccountId() {
			return accountId;
		}

		public void setAccountId(String accountId) {
			this.accountId = accountId;
		}

		public TransactionType getTransactionType() {
			return transactionType;
		}

		public void setTransactionType(TransactionType transactionType) {
			this.transactionType = transactionType;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public Integer getSequenceNumber() {
			return sequenceNumber;
		}

		public void setSequenceNumber(Integer sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}

	    
	

}
