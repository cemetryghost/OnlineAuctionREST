package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.LotService;
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
    private final LotService lotService;

    @Override
    public void run(ApplicationArguments args) {
        lotService.checkAndUpdateLotStatusDateClosing();
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            UserAccounts admin = new UserAccounts();
            admin.setName("Администратор");
            admin.setSurname("Администратор");
            admin.setLogin("admin");
            admin.setBirth_date(LocalDate.of(2004, 2, 3));
            admin.setEmail("admin_auction@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin159753"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(Status.ACTIVE);
            userRepository.save(admin);
        }
    }
}
