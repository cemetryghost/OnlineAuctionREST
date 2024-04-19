package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerNewUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    void blockUser(Long id);
    void unblockUser(Long id);
}
