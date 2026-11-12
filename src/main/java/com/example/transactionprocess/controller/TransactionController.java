package com.example.transactionprocess.controller;
import com.example.transactionprocess.dto.ProcessingSummary;
import com.example.transactionprocess.model.TransactionRequest;
import com.example.transactionprocess.model.TransactionStatus;
import com.example.transactionprocess.service.TransactionAsyncService;
import com.example.transactionprocess.service.TransactionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionAsyncService asyncService;
    private final TransactionService transactionService;

    public TransactionController(
            TransactionAsyncService asyncService,
            TransactionService transactionService) {

        this.asyncService = asyncService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionStatus> processTransaction(
            @RequestBody TransactionRequest request) {

        boolean accepted =
                transactionService.registerAcceptedRequest(request);

        if (!accepted) {
            TransactionStatus status =
                    transactionService.getRequestStatus(
                            request.getRequestId());

            if (status == null) {
                status = TransactionStatus.DUPLICATE;
            }

            return ResponseEntity.ok(status);
        }

        asyncService.processInBackground(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(TransactionStatus.RECEIVED);
    }

    @GetMapping("/summary")
    public ResponseEntity<ProcessingSummary> getProcessingSummary() {

        return ResponseEntity.ok(
                transactionService.getProcessingSummary()
        );
    }
    

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<TransactionStatus> getTransactionStatus(
            @PathVariable String transactionId) {

        TransactionStatus status =
                transactionService.getTransactionStatus(transactionId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

}
