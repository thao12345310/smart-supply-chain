package com.distribution.service.impl;

import com.distribution.dto.UserDTO;
import com.distribution.model.Role;
import com.distribution.model.User;
import com.distribution.repository.RoleRepository;
import com.distribution.repository.UserRepository;
import com.distribution.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private UserDTO toDto(User u) {
        Set<String> roleNames = u.getRoles() == null ? Set.of() :
                u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .active(u.isActive())
                .roles(roleNames)
                .build();
    }

    private Set<Role> resolveRoles(Set<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> normalized = requested.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(Collectors.toSet());
        return roleRepository.findByNameIn(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        User u = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDto(u);
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .active(dto.getActive() == null ? true : dto.getActive())
                .roles(resolveRoles(dto.getRoles()))
                .build();
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getActive() != null) user.setActive(dto.getActive());
        if (dto.getRoles() != null) user.setRoles(resolveRoles(dto.getRoles()));

        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO setActive(Long id, boolean active) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(active);
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        return roleRepository.findAll().stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
