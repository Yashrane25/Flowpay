package com.yashrane.flowpay_backend.repository;

import com.yashrane.flowpay_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
