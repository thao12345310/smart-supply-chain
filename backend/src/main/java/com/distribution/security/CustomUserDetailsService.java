package com.distribution.security;

import com.distribution.model.User;
import com.distribution.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService for Spring Security authentication
 * Loads user data from the database for authentication
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for authentication
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
        
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }
        
        return new CustomUserDetails(user);
    }

    /**
     * Load user by ID for token-based authentication
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id));
        
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is disabled: " + id);
        }
        
        return new CustomUserDetails(user);
    }
}
