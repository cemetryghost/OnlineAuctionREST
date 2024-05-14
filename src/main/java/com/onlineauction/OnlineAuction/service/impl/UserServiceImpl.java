package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;
    private final LotService lotService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, CustomUserDetailsServiceImpl customUserDetailsServiceImpl, LotService lotService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsServiceImpl = customUserDetailsServiceImpl;
        this.lotService = lotService;
    }

    @Override
    public UserDTO registerNewUser(UserDTO userDTO) {
        validateUserAge(userDTO.getBirth_date());
        checkDuplicateUser(userDTO.getLogin(), userDTO.getEmail());

        userDTO.setStatus(Status.ACTIVE);
        UserAccounts user = userMapper.userDTOToUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserAccounts savedUser = userRepository.save(user);
        customUserDetailsServiceImpl.updateUserDetails(savedUser);
        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь не найден"));
        return userMapper.userToUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        UserAccounts existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь не найден"));
        userMapper.updateUserFromDto(userDTO, existingUser);
        return userMapper.userToUserDTO(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long userId) {
        lotService.handleUserDeletion(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void blockUser(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь не найден"));
        user.setStatus(Status.BLOCKED);
        userRepository.save(user);
    }

    @Override
    public void unblockUser(Long id) {
        UserAccounts user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь не найден"));
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    private void validateUserAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        long age = ChronoUnit.YEARS.between(birthDate, today);
        if (age < 18) {
            throw new UserException("Регистрация на платформе доступна только с 18 лет!");
        }
    }

    private void checkDuplicateUser(String login, String email) {
        if (userRepository.existsByLogin(login)) {
            throw new UserException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserException("Пользователь с таким email уже существует");
        }
    }
}
