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
public class UserDTO {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Фамилия не может быть пустой")
    private String surname;

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Пароль должен быть равен 8 символам или более")
    private String password;

    @NotNull(message = "Дата рождения не может быть пустрой")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birth_date;

    @NotNull(message = "Роль не может быть пустой")
    private Role role;

    private Status status;
}
