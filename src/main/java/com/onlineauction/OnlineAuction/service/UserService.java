package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.exception.UserException;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDTO registerNewUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    void blockUser(Long id);
    void unblockUser(Long id);
    void initiateUserRegistration(UserDTO userDTO) throws UserException;
    void completeUserRegistration(UserDTO userDTO) throws UserException;
    Map<String, String> register(UserDTO userDTO) throws UserException;
}
