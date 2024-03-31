package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.AuthenticationDTO;
import com.onlineauction.OnlineAuction.dto.RegistrationDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.config.UserMapper;
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

    public void register(RegistrationDTO registrationDTO) {
        UserAccounts user = userMapper.registrationDtoToUserAccounts(registrationDTO);
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        userRepository.save(user);
    }

    public String authenticate(AuthenticationDTO authenticationDTO) {
        UserAccounts user = userRepository.findByLogin(authenticationDTO.getLogin());
        if (user != null && passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())) {
            return "Authentication successful";
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }
}
