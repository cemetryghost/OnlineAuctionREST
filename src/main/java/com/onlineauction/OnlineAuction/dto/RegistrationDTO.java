package com.onlineauction.OnlineAuction.dto;

import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.Status;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RegistrationDTO {
    private String name;
    private String surname;
    private String login;
    private String password;
    private LocalDate birth_date;
    private Role role;
    private Status status;
}
