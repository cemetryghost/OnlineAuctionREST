package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_Success() {
        UserAccounts user = new UserAccounts();
        user.setLogin("user");
        user.setPassword("password");
        user.setStatus(Status.ACTIVE);
        user.setRole(Role.SELLER);

        when(userRepository.findByLoginOrEmail("user")).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user");

        assertNotNull(userDetails);
        assertEquals("user", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELLER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByLoginOrEmail("user")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("user"));

        assertEquals("Пользователь с таким логином или почтой не найден: user", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserBlocked() {
        UserAccounts user = new UserAccounts();
        user.setLogin("user");
        user.setPassword("password");
        user.setStatus(Status.BLOCKED);
        user.setRole(Role.SELLER); // Устанавливаем роль

        when(userRepository.findByLoginOrEmail("user")).thenReturn(user);

        LockedException exception = assertThrows(LockedException.class, () -> customUserDetailsService.loadUserByUsername("user"));

        assertEquals("Ваш аккаунт заблокирован, свяжитесь с администратором по эл. почте: admin123@gmail.com", exception.getMessage());
    }

    @Test
    void testGetCurrentUserLogin_Authenticated() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        String currentUserLogin = customUserDetailsService.getCurrentUserLogin();

        assertEquals("user", currentUserLogin);
    }

    @Test
    void testGetCurrentUserLogin_Anonymous() {
        Authentication authentication = mock(AnonymousAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        String currentUserLogin = customUserDetailsService.getCurrentUserLogin();

        assertNull(currentUserLogin);
    }

    @Test
    void testUpdateUserDetails() {
        UserAccounts user = new UserAccounts();
        user.setLogin("user");
        user.setPassword("password");
        user.setRole(Role.SELLER);

        when(userRepository.findByLoginOrEmail("user")).thenReturn(user);

        customUserDetailsService.updateUserDetails(user);

        verify(userRepository, times(1)).findByLoginOrEmail("user");
    }
}