package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class ApplicationStartupRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!userRepository.existsByRole(Role.ADMINISTRATOR)) {
            UserAccounts admin = new UserAccounts();
            admin.setName("Matvey");
            admin.setSurname("Marusik");
            admin.setLogin("admin");
            admin.setBirth_date(LocalDate.of(2004, 2, 3));
            admin.setPassword(passwordEncoder.encode("159753"));
            admin.setRole(Role.ADMINISTRATOR);
            admin.setStatus(Status.ACTIVE);
            userRepository.save(admin);
        }
    }
}
