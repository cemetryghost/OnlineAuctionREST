package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import com.onlineauction.OnlineAuction.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Mock
    private LotService lotService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterNewUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        userDTO.setBirth_date(LocalDate.of(2000, 1, 1));
        userDTO.setStatus(Status.ACTIVE);

        UserAccounts user = new UserAccounts();
        user.setLogin("testUser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.existsByLogin(userDTO.getLogin())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userMapper.userDTOToUser(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.registerNewUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getLogin(), result.getLogin());
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(customUserDetailsServiceImpl, times(1)).updateUserDetails(user);
    }

    @Test
    void testRegisterNewUser_ThrowsUserException_DuplicateLogin() {
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("duplicateUser");
        userDTO.setEmail("test@example.com");
        userDTO.setBirth_date(LocalDate.of(2000, 1, 1));

        when(userRepository.existsByLogin(userDTO.getLogin())).thenReturn(true);

        UserException exception = assertThrows(UserException.class, () -> userService.registerNewUser(userDTO));

        assertEquals("Пользователь с таким логином уже существует", exception.getMessage());
    }

    @Test
    void testRegisterNewUser_ThrowsUserException_DuplicateEmail() {
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("testUser");
        userDTO.setEmail("duplicate@example.com");
        userDTO.setBirth_date(LocalDate.of(2000, 1, 1));

        when(userRepository.existsByLogin(userDTO.getLogin())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        UserException exception = assertThrows(UserException.class, () -> userService.registerNewUser(userDTO));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        UserAccounts user = new UserAccounts();
        user.setId(1L);
        user.setLogin("testUser");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setLogin("testUser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userDTO.getLogin(), result.getLogin());
    }

    @Test
    void testGetUserById_ThrowsUserException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () -> userService.getUserById(1L));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        UserAccounts existingUser = new UserAccounts();
        existingUser.setId(1L);
        existingUser.setLogin("testUser");

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("updatedUser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        doNothing().when(userMapper).updateUserFromDto(userDTO, existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.userToUserDTO(existingUser)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(1L, userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getLogin(), result.getLogin());
    }

    @Test
    void testDeleteUser_Success() {
        Long userId = 1L;

        doNothing().when(lotService).handleUserDeletion(userId);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(lotService, times(1)).handleUserDeletion(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testBlockUser_Success() {
        UserAccounts user = new UserAccounts();
        user.setId(1L);
        user.setStatus(Status.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.blockUser(1L);

        assertEquals(Status.BLOCKED, user.getStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUnblockUser_Success() {
        UserAccounts user = new UserAccounts();
        user.setId(1L);
        user.setStatus(Status.BLOCKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.unblockUser(1L);

        assertEquals(Status.ACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testValidateUserAge_ThrowsUserException_Underage() {
        LocalDate underageBirthDate = LocalDate.now().minusYears(17);

        UserDTO userDTO = new UserDTO();
        userDTO.setBirth_date(underageBirthDate);

        UserException exception = assertThrows(UserException.class, () -> userService.registerNewUser(userDTO));

        assertEquals("Регистрация на платформе доступна только с 18 лет!", exception.getMessage());
    }
}

