package com.distribution.controller;

import com.distribution.dto.*;
import com.distribution.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication, registration, and token operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed: " + ex.getMessage()));
        }
    }

    /**
     * User registration
     */
    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Registration successful"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get a new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Get current authenticated user info
     */
    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get information about the currently authenticated user")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser() {
        try {
            AuthResponse.UserInfo userInfo = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(userInfo));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
    }

    /**
     * Logout (client-side token removal, but can be extended for token blacklisting)
     */
    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logout user (invalidate token on client-side)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // In a stateless JWT setup, logout is handled client-side by removing the token
        // Could be extended to implement token blacklisting if needed
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    /**
     * Admin-only: Register new user (can assign any role)
     */
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin Register User", description = "Admin-only endpoint to register users with specific roles")
    public ResponseEntity<ApiResponse<AuthResponse>> adminRegister(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "User registered successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }
}
