package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserAccounts userAccounts = userRepository.findByLogin(login);
        if (userAccounts == null) {
            throw new UsernameNotFoundException("User not found with login: " + login);
        }
        if (userAccounts.getStatus() == Status.BLOCKED) {
            throw new RuntimeException("User account is blocked");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userAccounts.getRole().name());
        return User.builder()
                .username(userAccounts.getLogin())
                .password(userAccounts.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountLocked(userAccounts.getStatus() == Status.BLOCKED)
                .build();
    }
}

