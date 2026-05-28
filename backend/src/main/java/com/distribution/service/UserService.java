package com.distribution.service;

import com.distribution.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();
    UserDTO getById(Long id);
    UserDTO create(UserDTO dto);
    UserDTO update(Long id, UserDTO dto);
    UserDTO setActive(Long id, boolean active);
    void resetPassword(Long id, String newPassword);
    void delete(Long id);
    List<String> getAllRoleNames();
}
