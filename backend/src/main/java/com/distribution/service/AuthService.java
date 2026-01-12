package com.distribution.service;

import com.distribution.dto.AuthResponse;
import com.distribution.dto.LoginRequest;
import com.distribution.dto.RegisterRequest;
import com.distribution.dto.RefreshTokenRequest;
import com.distribution.model.Role;
import com.distribution.model.User;
import com.distribution.repository.RoleRepository;
import com.distribution.repository.UserRepository;
import com.distribution.security.CustomUserDetails;
import com.distribution.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles user authentication, registration, and token management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT tokens
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = tokenProvider.generateToken(userDetails);
            String refreshToken = tokenProvider.generateRefreshToken(userDetails);

            return createAuthResponse(userDetails, accessToken, refreshToken);
        } catch (BadCredentialsException ex) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + request.getUsername());
        }

        // Check if email exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + request.getEmail());
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<String> roleNames = request.getRoles().stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toSet());
            roles = roleRepository.findByNameIn(roleNames);
            
            if (roles.isEmpty()) {
                log.warn("No valid roles found for: {}", request.getRoles());
            }
        }

        // If no roles specified, assign default role (e.g., staff)
        if (roles.isEmpty()) {
            roleRepository.findByName("ROLE_SALES_STAFF")
                    .ifPresent(roles::add);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .active(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // Generate tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = tokenProvider.generateToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        return createAuthResponse(userDetails, accessToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is disabled");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = tokenProvider.generateToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

        return createAuthResponse(userDetails, newAccessToken, newRefreshToken);
    }

    /**
     * Get current authenticated user info
     */
    public AuthResponse.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return createUserInfo(userDetails);
    }

    /**
     * Create auth response from user details and tokens
     */
    private AuthResponse createAuthResponse(CustomUserDetails userDetails, 
                                            String accessToken, 
                                            String refreshToken) {
        return AuthResponse.success(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationMs(),
                createUserInfo(userDetails)
        );
    }

    /**
     * Create user info from user details
     */
    private AuthResponse.UserInfo createUserInfo(CustomUserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());

        return AuthResponse.UserInfo.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }
}
