package com.onlineauction.OnlineAuction.dto;

import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Surname cannot be empty")
    private String surname;

    @NotBlank(message = "Login cannot be empty")
    private String login;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Birth date cannot be empty")
    @Past(message = "Birth date must be in the past")
    private LocalDate birth_date;

    @NotNull(message = "Role cannot be empty")
    private Role role;

    private Status status;
}
