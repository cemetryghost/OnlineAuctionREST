package com.onlineauction.OnlineAuction;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {
        List<UserDTO> users = List.of(new UserDTO(), new UserDTO());
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().json("[{}, {}]"));
    }

    @Test
    void getUserById_ExistingId_ReturnsUser() throws Exception {
        Long id = 1L;
        UserDTO userDTO = new UserDTO();
        when(userService.getUserById(id)).thenReturn(userDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/{id}", id)
                        .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {
        Long id = 1L;
        UserDTO userDTO = new UserDTO();
        when(userService.updateUser(eq(id), any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                    .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void deleteUser_ReturnsNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/{id}", id)
                        .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void blockUser_ReturnsNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(userService).blockUser(id);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/{id}/block", id)
                        .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void unblockUser_ReturnsNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(userService).unblockUser(id);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/{id}/unblock", id)
                        .with(user("admin").password("Siilich312").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}
