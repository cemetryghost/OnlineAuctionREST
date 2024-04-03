package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ApplicationStartupRunnerConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            UserAccounts admin = new UserAccounts();
            admin.setName("Матвей");
            admin.setSurname("Марусик");
            admin.setLogin("admin");
            admin.setBirth_date(LocalDate.of(2004, 2, 3));
            admin.setPassword(passwordEncoder.encode("Silich312"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(Status.ACTIVE);
            userRepository.save(admin);
        }
    }
}
