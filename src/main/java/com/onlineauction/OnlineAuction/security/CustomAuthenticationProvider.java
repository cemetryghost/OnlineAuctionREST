package com.onlineauction.OnlineAuction.security;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsServiceImpl customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserAccounts userAccounts = customUserDetailsService.findUserAccountByUsername(username);

        switch (userAccounts.getStatus()) {
            case BLOCKED:
                throw new LockedException("Ваш аккаунт заблокирован, свяжиетсь с администратором по эл.почте: admin_auction@gmail.com");
            case UNCONFIRMED:
                throw new UserException("Вы не подтвердили почту при регистрации");
            default:
                if (!passwordEncoder.matches(password, userAccounts.getPassword())) {
                    throw new BadCredentialsException("Неверный логин/email или пароль. Пожалуйста, попробуйте снова");
                }
                break;
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userAccounts.getRole().name());
        return new UsernamePasswordAuthenticationToken(userAccounts.getLogin(), userAccounts.getPassword(), Collections.singletonList(authority));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
