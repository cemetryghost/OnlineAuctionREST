package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Slf4j
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomUserDetailsServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateUserDetails(UserAccounts user) {
        UserDetails userDetails = loadUserByUsername(user.getLogin());
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserAccounts userAccounts = userRepository.findByLogin(login);
        if (userAccounts == null) {
            throw new UsernameNotFoundException("User not found with login: " + login);
        }

        if (userAccounts.getStatus() == Status.BLOCKED) {
            throw new LockedException("Your account is blocked");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userAccounts.getRole().name());
        return org.springframework.security.core.userdetails.User
                .withUsername(userAccounts.getLogin())
                .password(userAccounts.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountLocked(userAccounts.getStatus() == Status.BLOCKED)
                .credentialsExpired(false)
                .accountExpired(false)
                .disabled(false)
                .build();
    }
    public String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }

}

