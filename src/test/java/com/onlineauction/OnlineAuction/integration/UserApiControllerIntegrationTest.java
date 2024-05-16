package com.onlineauction.OnlineAuction.integration;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class UserApiControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setLogin("testuser");
        userDTO.setEmail("testuser@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDTO.getId()))
                .andExpect(jsonPath("$[0].login").value(userDTO.getLogin()))
                .andExpect(jsonPath("$[0].email").value(userDTO.getEmail()));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDTO);

        mockMvc.perform(get("/user/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.login").value(userDTO.getLogin()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        when(userService.updateUser(anyLong(), any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"updatedUser\",\"email\":\"updateduser@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.login").value(userDTO.getLogin()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));

        verify(userService, times(1)).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/user/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockUser() throws Exception {
        doNothing().when(userService).blockUser(anyLong());

        mockMvc.perform(post("/user/{id}/block", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).blockUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUnblockUser() throws Exception {
        doNothing().when(userService).unblockUser(anyLong());

        mockMvc.perform(post("/user/{id}/unblock", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).unblockUser(1L);
    }
}

