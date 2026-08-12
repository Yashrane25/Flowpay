package com.yashrane.flowpay_backend.controller;

import com.yashrane.flowpay_backend.dto.LoginRequest;
import com.yashrane.flowpay_backend.dto.LoginResponse;
import com.yashrane.flowpay_backend.dto.MeResponse;
import com.yashrane.flowpay_backend.entity.User;
import com.yashrane.flowpay_backend.exception.UserNotFoundException;
import com.yashrane.flowpay_backend.repository.UserRepository;
import com.yashrane.flowpay_backend.service.AuthService;
import com.yashrane.flowpay_backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${flowpay.jwt.cookie-name}")
    private String cookieName;

    @Value("${flowpay.jwt.cookie-secure}")
    private boolean cookieSecure;

    public AuthController(AuthService authService, JwtService jwtService, UserRepository userRepository){
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Log in",
            description = "On success, sets an httpOnly JWT cookie via Set Cookie. " +
                    "The response body does NOT contain the token - it is never " +
                    "exposed to JavaScript by design. Subsequent requests from a " +
                    "browser automatically include this cookie."
    )

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        String token = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)          //JavaScript cannot read this cookie at all.
                .secure(cookieSecure)
                .sameSite("Lax")         //sent on top level navigation and same site fetches, blocks most cross site forgery vectors
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new LoginResponse("Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new LoginResponse("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(null));

//        return ResponseEntity.ok(new MeResponse(email, user.getFullName(), role));
        return ResponseEntity.ok(new MeResponse(user.getFullName(), email, role));
    }
}
