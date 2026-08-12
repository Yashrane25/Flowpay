package com.yashrane.flowpay_backend.service;

/*
   Unit tests for TransferService using JUnit and Mockito. Mock the repository dependencies so
   the tests don't require a database. Provide controlled scenarios such as insufficient balance
   or a missing user and verify that TransferService throws the expected business exception.
 */

import com.yashrane.flowpay_backend.dto.TransferRequest;
import com.yashrane.flowpay_backend.entity.User;
import com.yashrane.flowpay_backend.entity.Wallet;
import com.yashrane.flowpay_backend.exception.InsufficientBalanceException;
import com.yashrane.flowpay_backend.exception.UserNotFoundException;
import com.yashrane.flowpay_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock private UserRepository userRepository;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferThrowsWhenBalanceInsufficient() {
        User senderUser = new User("Test Sender", "sender@flowpay.com", "hashedpw");
        Wallet senderWallet = new Wallet(senderUser);
        senderWallet.setBalance(new BigDecimal("50.00"));
        ReflectionTestUtils.setField(senderWallet, "id", 1L);

        User receiverUser = new User("Receiver", "r@flowpay.com", "hashedpw");
        Wallet receiverWallet = new Wallet(receiverUser);
        receiverWallet.setBalance(BigDecimal.ZERO);
        ReflectionTestUtils.setField(receiverWallet, "id", 2L);

        when(idempotencyRecordRepository.findByIdempotencyKey(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("sender@flowpay.com"))
                .thenReturn(Optional.of(senderUser));
        when(walletRepository.findByUser(senderUser))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIdForUpdate(eq(1L)))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIdForUpdate(eq(2L)))
                .thenReturn(Optional.of(receiverWallet));

        TransferRequest request = new TransferRequest();
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("500.00"));

        assertThrows(InsufficientBalanceException.class, () ->
                transferService.transfer("sender@flowpay.com", request, "test-key-unit-1")
        );
    }

    @Test
    void transferThrowsWhenSenderUserNotFound() {
        when(idempotencyRecordRepository.findByIdempotencyKey(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost@flowpay.com"))
                .thenReturn(Optional.empty()); // simulate: no such user exists

        TransferRequest request = new TransferRequest();
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("10.00"));

        assertThrows(UserNotFoundException.class, () ->
                transferService.transfer("ghost@flowpay.com", request, "test-key-unit-2")
        );
    }
}
