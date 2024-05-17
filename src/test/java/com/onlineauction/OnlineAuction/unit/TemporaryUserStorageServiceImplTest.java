package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.impl.TemporaryUserStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TemporaryUserStorageServiceImplTest {

    private TemporaryUserStorageServiceImpl temporaryUserStorageService;

    @BeforeEach
    public void setUp() {
        temporaryUserStorageService = new TemporaryUserStorageServiceImpl();
    }

    @Test
    public void testSaveAndRetrieveTemporaryUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");

        temporaryUserStorageService.saveTemporaryUser("test@example.com", userDTO);
        UserDTO retrievedUser = temporaryUserStorageService.getTemporaryUser("test@example.com");

        assertNotNull(retrievedUser);
        assertEquals("test@example.com", retrievedUser.getEmail());
    }

    @Test
    public void testRemoveTemporaryUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");

        temporaryUserStorageService.saveTemporaryUser("test@example.com", userDTO);
        temporaryUserStorageService.removeTemporaryUser("test@example.com");

        UserDTO retrievedUser = temporaryUserStorageService.getTemporaryUser("test@example.com");
        assertNull(retrievedUser);
    }
}