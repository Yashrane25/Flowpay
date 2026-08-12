package com.yashrane.flowpay_backend.concurrency;

/*
  Integration test using JUnit and Spring Boot to verify that concurrent transfers from the same
  wallet don't cause a race condition or lost balance updates. Used the real database and run two
  transfers simultaneously using multiple threads, then verify that the sender's final balance is
  correct, proving that the pessimistic locking works.
*/

import com.yashrane.flowpay_backend.dto.TransferRequest;
import com.yashrane.flowpay_backend.entity.User;
import com.yashrane.flowpay_backend.entity.Wallet;
import com.yashrane.flowpay_backend.repository.UserRepository;
import com.yashrane.flowpay_backend.repository.WalletRepository;
import com.yashrane.flowpay_backend.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransferConcurrencyTest {
    @Autowired private TransferService transferService;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void concurrentTransfersFromSameWalletDoNotLoseUpdates() throws InterruptedException {
        User sender = userRepository.save(
                new User("Race Sender", "race-sender-" + UUID.randomUUID() + "@flowpay.com",
                        passwordEncoder.encode("test1234")));
        Wallet senderWallet = walletRepository.save(new Wallet(sender));
        senderWallet.setBalance(new BigDecimal("1000.00"));
        walletRepository.save(senderWallet);

        User receiverA = userRepository.save(
                new User("Receiver A", "receiver-a-" + UUID.randomUUID() + "@flowpay.com",
                        passwordEncoder.encode("test1234")));
        Wallet receiverAWallet = walletRepository.save(new Wallet(receiverA));

        User receiverB = userRepository.save(
                new User("Receiver B", "receiver-b-" + UUID.randomUUID() + "@flowpay.com",
                        passwordEncoder.encode("test1234")));
        Wallet receiverBWallet = walletRepository.save(new Wallet(receiverB));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Throwable> capturedErrors = new CopyOnWriteArrayList<>();

        Runnable transferToA = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                TransferRequest request = new TransferRequest();
                request.setToWalletId(receiverAWallet.getId());
                request.setAmount(new BigDecimal("300.00"));
                transferService.transfer(sender.getEmail(), request, "race-key-A-" + UUID.randomUUID());
            } catch (Throwable e) {
                capturedErrors.add(e);
            } finally {
                doneLatch.countDown();
            }
        };

        Runnable transferToB = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                TransferRequest request = new TransferRequest();
                request.setToWalletId(receiverBWallet.getId());
                request.setAmount(new BigDecimal("200.00"));
                transferService.transfer(sender.getEmail(), request, "race-key-B-" + UUID.randomUUID());
            } catch (Throwable e) {
                capturedErrors.add(e);
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(transferToA);
        executor.submit(transferToB);

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        for (Throwable error : capturedErrors) {
            error.printStackTrace();
        }

        Wallet finalSenderWallet = walletRepository.findById(senderWallet.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("500.0000").compareTo(finalSenderWallet.getBalance()));
    }
}
