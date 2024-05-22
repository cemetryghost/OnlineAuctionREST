package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.mapper.UserMapper;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.*;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import com.onlineauction.OnlineAuction.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

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

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private TemporaryUserStorageService temporaryUserStorageService;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private UserDTO userDTO;
    private UserAccounts userAccounts;

    @BeforeEach
    public void setUp() {
        userDTO = new UserDTO();
        userDTO.setLogin("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        userDTO.setBirth_date(LocalDate.of(2000, 1, 1));
        userDTO.setVerificationCode("123456");

        userAccounts = new UserAccounts();
        userAccounts.setId(1L);
        userAccounts.setLogin("testuser");
        userAccounts.setEmail("test@example.com");
        userAccounts.setPassword("password");
        userAccounts.setBirth_date(LocalDate.of(2000, 1, 1));
        userAccounts.setRole(Role.SELLER);
        userAccounts.setStatus(Status.ACTIVE);
    }

    @Test
    public void testRegisterNewUser() {
        when(userMapper.userDTOToUser(any(UserDTO.class))).thenReturn(userAccounts);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserAccounts.class))).thenReturn(userAccounts);
        when(userMapper.userToUserDTO(any(UserAccounts.class))).thenReturn(userDTO);

        UserDTO result = userServiceImpl.registerNewUser(userDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userAccounts));
        when(userMapper.userToUserDTO(any(UserAccounts.class))).thenReturn(userDTO);

        UserDTO result = userServiceImpl.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }

    @Test
    public void testGetUserById_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userServiceImpl.getUserById(1L));
    }

    @Test
    public void testUpdateUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userAccounts));
        when(userMapper.userToUserDTO(any(UserAccounts.class))).thenReturn(userDTO);
        when(userRepository.save(any(UserAccounts.class))).thenReturn(userAccounts);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setLogin("updatedUser");

        UserDTO result = userServiceImpl.updateUser(1L, updatedUser);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
    }

    @Test
    public void testDeleteUser() {
        doNothing().when(lotService).handleUserDeletion(anyLong());
        doNothing().when(userRepository).deleteById(anyLong());

        userServiceImpl.deleteUser(1L);

        verify(lotService, times(1)).handleUserDeletion(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testBlockUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userAccounts));

        userServiceImpl.blockUser(1L);

        assertEquals(Status.BLOCKED, userAccounts.getStatus());
        verify(userRepository, times(1)).save(userAccounts);
    }

    @Test
    public void testUnblockUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userAccounts));

        userServiceImpl.unblockUser(1L);

        assertEquals(Status.ACTIVE, userAccounts.getStatus());
        verify(userRepository, times(1)).save(userAccounts);
    }

    @Test
    public void testValidateUserAge_Underage() {
        LocalDate underageBirthDate = LocalDate.now().minusYears(17);

        assertThrows(UserException.class, () -> userServiceImpl.validateUserAge(underageBirthDate));
    }

    @Test
    public void testCheckDuplicateUser_DuplicateLogin() {
        when(userRepository.existsByLogin(anyString())).thenReturn(true);

        assertThrows(UserException.class, () -> userServiceImpl.checkDuplicateUser("testuser", "test@example.com"));
    }

    @Test
    public void testCheckDuplicateUser_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserException.class, () -> userServiceImpl.checkDuplicateUser("testuser", "test@example.com"));
    }

    @Test
    public void testRegister_WithVerificationCode() throws UserException {
        when(temporaryUserStorageService.getTemporaryUser(anyString())).thenReturn(userDTO);
        when(verificationCodeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.userDTOToUser(any(UserDTO.class))).thenReturn(userAccounts); // Add this line
        doNothing().when(temporaryUserStorageService).removeTemporaryUser(anyString());

        Map<String, String> result = userServiceImpl.register(userDTO);

        assertNotNull(result);
        assertEquals("Регистрация прошла успешно!", result.get("message"));
    }

    @Test
    public void testRegister_WithoutVerificationCode() throws UserException {
        userDTO.setVerificationCode(null);
        doNothing().when(temporaryUserStorageService).saveTemporaryUser(anyString(), any(UserDTO.class));
        when(verificationCodeService.generateCode(anyString())).thenReturn("123456");
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        Map<String, String> result = userServiceImpl.register(userDTO);

        assertNotNull(result);
        assertEquals("Код подтверждения отправлен на ваш email!", result.get("message"));
    }

    @Test
    public void testInitiateUserRegistration() throws UserException {
        doNothing().when(temporaryUserStorageService).saveTemporaryUser(anyString(), any(UserDTO.class));
        when(verificationCodeService.generateCode(anyString())).thenReturn("123456");
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        userServiceImpl.initiateUserRegistration(userDTO);

        verify(temporaryUserStorageService, times(1)).saveTemporaryUser(anyString(), any(UserDTO.class));
        verify(verificationCodeService, times(1)).generateCode(anyString());
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testCompleteUserRegistration() throws UserException {
        when(verificationCodeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(temporaryUserStorageService.getTemporaryUser(anyString())).thenReturn(userDTO);
        when(userMapper.userDTOToUser(any(UserDTO.class))).thenReturn(userAccounts); // Add this line
        doNothing().when(temporaryUserStorageService).removeTemporaryUser(anyString());

        userServiceImpl.completeUserRegistration(userDTO);

        verify(verificationCodeService, times(1)).validateCode(anyString(), anyString());
        verify(temporaryUserStorageService, times(1)).getTemporaryUser(anyString());
        verify(temporaryUserStorageService, times(1)).removeTemporaryUser(anyString());
    }

    @Test
    public void testCompleteUserRegistration_InvalidCode() {
        when(verificationCodeService.validateCode(anyString(), anyString())).thenReturn(false);

        assertThrows(UserException.class, () -> userServiceImpl.completeUserRegistration(userDTO));

        verify(verificationCodeService, times(1)).validateCode(anyString(), anyString());
        verify(temporaryUserStorageService, never()).getTemporaryUser(anyString());
        verify(temporaryUserStorageService, never()).removeTemporaryUser(anyString());
    }

    @Test
    public void testCompleteUserRegistration_TemporaryUserNotFound() {
        when(verificationCodeService.validateCode(anyString(), anyString())).thenReturn(true);
        when(temporaryUserStorageService.getTemporaryUser(anyString())).thenReturn(null);

        assertThrows(UserException.class, () -> userServiceImpl.completeUserRegistration(userDTO));

        verify(verificationCodeService, times(1)).validateCode(anyString(), anyString());
        verify(temporaryUserStorageService, times(1)).getTemporaryUser(anyString());
        verify(temporaryUserStorageService, never()).removeTemporaryUser(anyString());
    }

    @Test
    public void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userAccounts));
        when(userMapper.userToUserDTO(any(UserAccounts.class))).thenReturn(userDTO);

        List<UserDTO> result = userServiceImpl.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getLogin());
    }

    @Test
    public void testGetAllUsers_NoAdmin() {
        UserAccounts adminUser = new UserAccounts();
        adminUser.setRole(Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(userAccounts, adminUser));
        when(userMapper.userToUserDTO(userAccounts)).thenReturn(userDTO);

        List<UserDTO> result = userServiceImpl.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getLogin());
    }
}

