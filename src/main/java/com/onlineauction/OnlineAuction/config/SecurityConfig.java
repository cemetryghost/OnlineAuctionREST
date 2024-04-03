package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    private final CustomUserDetailsService customUserDetailsService;
    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(authz -> authz
                        .requestMatchers("/", "/auth/login","/auth/register", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Разрешаем доступ к этим путям
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Только для администраторов
                        .requestMatchers("/seller/**").hasRole("SELLER") // Только для продавцов
                        .requestMatchers("/buyer/**").hasRole("BUYER") // Только для покупателей
                        .anyRequest().authenticated() // Для всех остальных запросов требуем аутентификацию
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login") // URL для отправки логина и пароля
                        .usernameParameter("login") // Параметр для логина
                        .passwordParameter("password") // Параметр для пароля
                        .failureUrl("/auth/login?error=true") // Перенаправление при ошибке
                        .defaultSuccessUrl("/welcome", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // URL для выхода
                        .logoutSuccessUrl("/auth/login?logout") // Страница после выхода
                        .permitAll());

        return http.build();
    }
}
