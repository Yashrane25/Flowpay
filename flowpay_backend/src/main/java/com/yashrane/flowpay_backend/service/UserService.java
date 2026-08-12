package com.yashrane.flowpay_backend.service;

import com.yashrane.flowpay_backend.dto.RegisterUserRequest;
import com.yashrane.flowpay_backend.dto.UserResponse;
import com.yashrane.flowpay_backend.entity.User;
import com.yashrane.flowpay_backend.entity.Wallet;
import com.yashrane.flowpay_backend.repository.UserRepository;
import com.yashrane.flowpay_backend.repository.WalletRepository;
import com.yashrane.flowpay_backend.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request){

        String hashedPassword = passwordEncoder.encode((request.getPassword()));

        User user = new User(request.getFullName(), request.getEmail(), hashedPassword);
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet(savedUser);
        Wallet savedWallet = walletRepository.save(wallet);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedWallet.getBalance()
        );
    }

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        Wallet wallet = walletRepository.findByUser(user).orElseThrow(() -> new UserNotFoundException(id));

        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), wallet.getBalance());
    }
}
