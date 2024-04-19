package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, CustomUserDetailsServiceImpl customUserDetailsServiceImpl) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsServiceImpl = customUserDetailsServiceImpl;
    }

    @Override
    public UserDTO registerNewUser(UserDTO userDTO) {
        if (userRepository.existsByLogin(userDTO.getLogin())) {
            throw new RuntimeException("Пользователь с таким логином уже сузествует");
        }
        userDTO.setStatus(Status.ACTIVE);
        UserAccounts user = userMapper.userDTOToUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserAccounts savedUser = userRepository.save(user);
        customUserDetailsServiceImpl.updateUserDetails(savedUser);
        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return userMapper.userToUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        UserAccounts existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userMapper.updateUserFromDto(userDTO, existingUser);
        return userMapper.userToUserDTO(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void blockUser(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setStatus(Status.BLOCKED);
        userRepository.save(user);
    }

    @Override
    public void unblockUser(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }
}
