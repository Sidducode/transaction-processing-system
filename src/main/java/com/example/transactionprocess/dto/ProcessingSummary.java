package com.example.transactionprocess.dto;

public class ProcessingSummary {

    private int totalReceived;
    private int processed;
    private int duplicates;
    private int failed;
    private int pending;

    public ProcessingSummary() {
    }

    public ProcessingSummary(
            int totalReceived,
            int processed,
            int duplicates,
            int failed,
            int pending) {

        this.totalReceived = totalReceived;
        this.processed = processed;
        this.duplicates = duplicates;
        this.failed = failed;
        this.pending = pending;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }
}
