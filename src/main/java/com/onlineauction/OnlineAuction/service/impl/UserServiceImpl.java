package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;
    private final LotService lotService;
    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;
    private final TemporaryUserStorageService temporaryUserStorageService;

    @Override
    public UserDTO registerNewUser(UserDTO userDTO) {
        userDTO.setStatus(Status.ACTIVE);
        UserAccounts user = userMapper.userDTOToUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserAccounts savedUser = userRepository.save(user);
        return userMapper.userToUserDTO(savedUser);
    }

    @Override
    public Map<String, String> register(UserDTO userDTO) throws UserException {
        if (userDTO.getVerificationCode() == null || userDTO.getVerificationCode().isEmpty()) {
            initiateUserRegistration(userDTO);
            return Map.of("message", "Код подтверждения отправлен на ваш email!");
        } else {
            completeUserRegistration(userDTO);
            return Map.of("message", "Регистрация прошла успешно!");
        }
    }

    public void initiateUserRegistration(UserDTO userDTO) throws UserException {
        validateUserAge(userDTO.getBirth_date());
        checkDuplicateUser(userDTO.getLogin(), userDTO.getEmail());

        String verificationCode = verificationCodeService.generateCode(userDTO.getEmail());
        emailService.sendEmail(userDTO.getEmail(), "Код подтверждения", "Ваш код подтверждения для регистрации на платформе: " + verificationCode);

        temporaryUserStorageService.saveTemporaryUser(userDTO.getEmail(), userDTO);
    }

    public void completeUserRegistration(UserDTO userDTO) throws UserException {
        if (verificationCodeService.validateCode(userDTO.getEmail(), userDTO.getVerificationCode())) {
            UserDTO tempUser = temporaryUserStorageService.getTemporaryUser(userDTO.getEmail());
            if (tempUser != null) {
                registerNewUser(tempUser);
                temporaryUserStorageService.removeTemporaryUser(userDTO.getEmail());
            } else {
                throw new UserException("Временные данные пользователя не найдены");
            }
        } else {
            throw new UserException("Неверный или истекший код подтверждения");
        }
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

    public void validateUserAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        long age = ChronoUnit.YEARS.between(birthDate, today);
        if (age < 18) {
            throw new UserException("Регистрация на платформе доступна только с 18 лет!");
        }
    }

    public void checkDuplicateUser(String login, String email) {
        if (userRepository.existsByLogin(login)) {
            throw new UserException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserException("Пользователь с таким email уже существует");
        }
    }
}
