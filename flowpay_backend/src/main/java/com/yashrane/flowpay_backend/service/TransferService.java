package com.yashrane.flowpay_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yashrane.flowpay_backend.dto.TransferRequest;
import com.yashrane.flowpay_backend.dto.TransferResponse;
import com.yashrane.flowpay_backend.entity.*;
import com.yashrane.flowpay_backend.event.TransferCompletedEvent;
import com.yashrane.flowpay_backend.exception.DuplicateRequestInFlightException;
import com.yashrane.flowpay_backend.exception.InsufficientBalanceException;
import com.yashrane.flowpay_backend.exception.UserNotFoundException;
import com.yashrane.flowpay_backend.exception.WalletNotFoundException;
import com.yashrane.flowpay_backend.messaging.TransferEventPublisher;
import com.yashrane.flowpay_backend.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;

@Service
public class TransferService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository; // NEW
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final TransferEventPublisher transferEventPublisher;

    public TransferService(WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           LedgerEntryRepository ledgerEntryRepository,
                           IdempotencyRecordRepository idempotencyRecordRepository,
                           ObjectMapper objectMapper,
                           UserRepository userRepository,
                           WalletService walletService,
                           TransferEventPublisher transferEventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.transferEventPublisher = transferEventPublisher;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResponse transfer(String senderEmail, TransferRequest request, String idempotencyKey) {

        var existing = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent() && "COMPLETED".equals(existing.get().getStatus())) {
            return deserialize(existing.get().getResponseBody());
        }

        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, "PROCESSING");
        try {
            idempotencyRecordRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRequestInFlightException(idempotencyKey);
        }

        User senderUser = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserNotFoundException(null));
        Wallet senderWalletLookup = walletRepository.findByUser(senderUser)
                .orElseThrow(() -> new UserNotFoundException(senderUser.getId()));

        Long fromId = senderWalletLookup.getId();
        Long toId = request.getToWalletId();

        Wallet firstLocked;
        Wallet secondLocked;

        if (fromId < toId) {
            firstLocked = walletRepository.findByIdForUpdate(fromId)
                    .orElseThrow(() -> new WalletNotFoundException(fromId));
            secondLocked = walletRepository.findByIdForUpdate(toId)
                    .orElseThrow(() -> new WalletNotFoundException(toId));
        } else {
            firstLocked = walletRepository.findByIdForUpdate(toId)
                    .orElseThrow(() -> new WalletNotFoundException(toId));
            secondLocked = walletRepository.findByIdForUpdate(fromId)
                    .orElseThrow(() -> new WalletNotFoundException(fromId));
        }

        Wallet fromWallet = fromId.equals(firstLocked.getId()) ? firstLocked : secondLocked;
        Wallet toWallet = toId.equals(firstLocked.getId()) ? firstLocked : secondLocked;

        BigDecimal amount = request.getAmount();

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(fromWallet.getId());
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        walletService.evictBalance(fromWallet.getId());
        walletService.evictBalance(toWallet.getId());

        transferEventPublisher.publishTransferCompleted(
                new TransferCompletedEvent(fromWallet.getId(), toWallet.getId(), amount)
        );

        Transaction transaction = transactionRepository.save(new Transaction("COMPLETED"));
        ledgerEntryRepository.save(new LedgerEntry(transaction, fromWallet, EntryType.DEBIT, amount));
        ledgerEntryRepository.save(new LedgerEntry(transaction, toWallet, EntryType.CREDIT, amount));

        TransferResponse response = new TransferResponse(
                fromWallet.getId(), toWallet.getId(), amount,
                fromWallet.getBalance(), toWallet.getBalance()
        );

        record.setStatus("COMPLETED");
        record.setResponseBody(serialize(response));
        idempotencyRecordRepository.save(record);

        return response;
    }

    private String serialize(TransferResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize transfer response", e);
        }
    }

    private TransferResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, TransferResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize stored transfer response", e);
        }
    }
}
