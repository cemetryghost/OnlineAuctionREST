package com.onlineauction.OnlineAuction.dto;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuthResDTO {
    private final String accessToken;
    private String refreshToken;
    private String role;
}
