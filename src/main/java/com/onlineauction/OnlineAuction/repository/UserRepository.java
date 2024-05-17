package com.onlineauction.OnlineAuction.repository;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccounts, Long> {
    UserAccounts findByLogin(String login);
    Optional<UserAccounts> findByEmail(String email);
    boolean existsByRole(Role role);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM UserAccounts u WHERE u.login = ?1 OR u.email = ?1")
    UserAccounts findByLoginOrEmail(String username);
}
