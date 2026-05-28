package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.UserDTO;
import com.distribution.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin user management endpoints.
 * Access restricted to ROLE_ADMIN via SecurityConfig (/api/users/**).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Users", description = "Admin user management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    @GetMapping("/roles")
    @Operation(summary = "List all roles available in the system")
    public ResponseEntity<ApiResponse<List<String>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllRoleNames()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<ApiResponse<UserDTO>> create(@Valid @RequestBody UserDTO dto) {
        UserDTO created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user (full name, email, roles, active)")
    public ResponseEntity<ApiResponse<UserDTO>> update(@PathVariable Long id,
                                                       @RequestBody UserDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, dto),
                "User updated successfully"));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<ApiResponse<UserDTO>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.setActive(id, true),
                "User activated"));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<ApiResponse<UserDTO>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.setActive(id, false),
                "User deactivated"));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Reset user password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }
}
