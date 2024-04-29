package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserAccounts, Long> {
    UserAccounts findByLogin(String login);
    boolean existsByRole(Role role);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
}
