package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.AuthenticationDTO;
import com.onlineauction.OnlineAuction.dto.RegistrationDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserAccounts registerNewUserAccount(RegistrationDTO registrationDto) {
        if (userRepository.existsByLogin(registrationDto.getLogin())) {
            throw new RuntimeException("Login already exists: " + registrationDto.getLogin());
        }
        UserAccounts user = userMapper.registrationDtoToUserAccounts(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        return userRepository.save(user);
    }

    public boolean authenticate(AuthenticationDTO authenticationDTO) {
        UserAccounts user = userRepository.findByLogin(authenticationDTO.getLogin());
        if (user == null) {
            return false;
        }
        if (user.getStatus() == Status.BLOCKED) {
            throw new RuntimeException("Account is blocked.");
        }
        boolean passwordMatch = passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword());
        return passwordMatch && (user.getStatus() == Status.ACTIVE);
    }
}