package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.UserDTO;

public interface TemporaryUserStorageService {
    void saveTemporaryUser(String email, UserDTO userDTO);
    UserDTO getTemporaryUser(String email);
    void removeTemporaryUser(String email);
}

