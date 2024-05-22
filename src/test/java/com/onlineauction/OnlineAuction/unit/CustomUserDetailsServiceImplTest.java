package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    private UserAccounts userAccounts;

    @BeforeEach
    public void setUp() {
        userAccounts = new UserAccounts();
        userAccounts.setLogin("testuser");
        userAccounts.setPassword("password");
        userAccounts.setStatus(Status.ACTIVE);
        userAccounts.setRole(Role.SELLER);
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByLoginOrEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsServiceImpl.loadUserByUsername("nonexistentuser"));
    }

    @Test
    public void testLoadUserByUsername_UserBlocked() {
        userAccounts.setStatus(Status.BLOCKED);
        when(userRepository.findByLoginOrEmail(anyString())).thenReturn(userAccounts);

        assertThrows(LockedException.class, () -> customUserDetailsServiceImpl.loadUserByUsername("testuser"));
    }

    @Test
    public void testLoadUserByUsername_UserNotConfirmed() {
        userAccounts.setStatus(Status.UNCONFIRMED);
        when(userRepository.findByLoginOrEmail(anyString())).thenReturn(userAccounts);

        assertThrows(UserException.class, () -> customUserDetailsServiceImpl.loadUserByUsername("testuser"));
    }

    @Test
    public void testLoadUserByUsername_Success() {
        when(userRepository.findByLoginOrEmail(anyString())).thenReturn(userAccounts);

        UserDetails userDetails = customUserDetailsServiceImpl.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + userAccounts.getRole().name())));
    }

    @Test
    public void testGetCurrentUserLogin_Authenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();

        assertEquals("testuser", currentUserLogin);
    }

    @Test
    public void testGetCurrentUserLogin_Anonymous() {
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();

        assertNull(currentUserLogin);
    }

    @Test
    public void testGetCurrentUserLogin_NullAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();

        assertNull(currentUserLogin);
    }
}