package com.yashrane.flowpay_backend.controller;

import com.yashrane.flowpay_backend.dto.LedgerEntryResponse;
import com.yashrane.flowpay_backend.dto.WalletBalanceResponse;
import com.yashrane.flowpay_backend.entity.LedgerEntry;
import com.yashrane.flowpay_backend.entity.User;
import com.yashrane.flowpay_backend.entity.Wallet;
import com.yashrane.flowpay_backend.exception.UserNotFoundException;
import com.yashrane.flowpay_backend.repository.LedgerEntryRepository;
import com.yashrane.flowpay_backend.repository.UserRepository;
import com.yashrane.flowpay_backend.repository.WalletRepository;
import com.yashrane.flowpay_backend.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletController(UserRepository userRepository,
                            WalletRepository walletRepository,
                            WalletService walletService,
                            LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletBalanceResponse> getMyWallet(Authentication authentication) {
        Wallet wallet = resolveMyWallet(authentication);
        BigDecimal balance = walletService.getBalance(wallet.getId());
        return ResponseEntity.ok(new WalletBalanceResponse(wallet.getId(), balance));
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<LedgerEntryResponse>> getMyTransactionHistory(Authentication authentication) {
        Wallet wallet = resolveMyWallet(authentication);
        List<LedgerEntry> entries = ledgerEntryRepository.findByWalletOrderByCreatedAtDesc(wallet);

        List<LedgerEntryResponse> response = entries.stream()
                .map(entry -> new LedgerEntryResponse(
                        entry.getType().name(),
                        entry.getAmount(),
                        entry.getCreatedAt(),
                        resolveCounterpartyName(entry, wallet)
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    private Wallet resolveMyWallet(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(null));
        return walletRepository.findByUser(user).orElseThrow(() -> new UserNotFoundException(user.getId()));
    }

    private String resolveCounterpartyName(LedgerEntry entry, Wallet myWallet) {
        List<LedgerEntry> siblings = ledgerEntryRepository.findByTransaction(entry.getTransaction());
        for (LedgerEntry sibling : siblings) {
            if (!sibling.getWallet().getId().equals(myWallet.getId())) {
                return sibling.getWallet().getUser().getFullName();
            }
        }
        return "Unknown";
    }
}
