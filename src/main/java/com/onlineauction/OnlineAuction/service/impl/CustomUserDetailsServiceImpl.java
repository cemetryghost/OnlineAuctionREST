package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccounts userAccounts = userRepository.findByLoginOrEmail(username);

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

    public UserAccounts findUserAccountByUsername(String username) {
        UserAccounts userAccounts = userRepository.findByLoginOrEmail(username);
        if (userAccounts == null) {
            throw new UsernameNotFoundException("Пользователь с таким логином/email не найден");
        }
        return userAccounts;
    }

    public String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }
}