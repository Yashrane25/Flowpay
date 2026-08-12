package com.yashrane.flowpay_backend.controller;

import com.yashrane.flowpay_backend.dto.UserResponse;
import com.yashrane.flowpay_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService){
        this.userService = userService;
    }

    @Operation(
            summary = "Get any user by ID (admin only)",
            description = "Requires ADMIN role. Returns 403 for authenticated users without " +
                    "the ADMIN role, distinct from 401 for unauthenticated requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Authenticated, but caller lacks ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user with the given ID")
    })

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getAnyUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
