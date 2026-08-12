package com.yashrane.flowpay_backend.controller;

import com.yashrane.flowpay_backend.dto.TransferRequest;
import com.yashrane.flowpay_backend.dto.TransferResponse;
import com.yashrane.flowpay_backend.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService){
        this.transferService = transferService;
    }

    @Operation(
            summary = "Transfer funds between wallets",
            description = "Debits the authenticated user's own wallet and credits the " +
                    "specified recipient wallet. Requires a unique Idempotency-Key header " +
                    "per distinct transfer attempt - reusing a key returns the original " +
                    "result instead of re-executing the transfer, protecting against " +
                    "duplicate submissions from retries."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. non positive amount)"),
            @ApiResponse(responseCode = "404", description = "Recipient wallet not found"),
            @ApiResponse(responseCode = "409", description = "A request with this Idempotency Key is already being processed"),
            @ApiResponse(responseCode = "422", description = "Insufficient balance in sender's wallet"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded for this user")
    })

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        String senderEmail = authentication.getName();
        return ResponseEntity.ok(transferService.transfer(senderEmail, request, idempotencyKey));
    }
}
