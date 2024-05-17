package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserNotConfirmedException;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateUserDetails(UserAccounts user) {
        loadUserByUsername(user.getLogin());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserAccounts userAccounts = userRepository.findByLoginOrEmail(username);
            if (userAccounts == null) {
                throw new UsernameNotFoundException("Пользователь с таким логином или почтой не найден: " + username);
            }

            if (userAccounts.getStatus() == Status.BLOCKED) {
                throw new LockedException("Ваш аккаунт заблокирован, свяжитесь с администратором по эл. почте: admin_auction@gmail.com");
            }

            if (userAccounts.getStatus() == Status.UNCONFIRMED) {
                throw new UserNotConfirmedException("User is not confirmed");
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
        } catch (UserNotConfirmedException ex) {
            throw new UserNotConfirmedException("User is not confirmed");
        }
    }
    public String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }
}