package com.yashrane.flowpay_backend.repository;

import com.yashrane.flowpay_backend.entity.LedgerEntry;
import com.yashrane.flowpay_backend.entity.Transaction;
import com.yashrane.flowpay_backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByWalletOrderByCreatedAtDesc(Wallet wallet);
    List<LedgerEntry> findByTransaction(Transaction transaction);
}
