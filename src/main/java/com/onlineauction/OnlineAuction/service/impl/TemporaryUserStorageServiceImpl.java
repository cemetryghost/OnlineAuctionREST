package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.TemporaryUserStorageService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
public class TemporaryUserStorageServiceImpl implements TemporaryUserStorageService {

    private final ConcurrentMap<String, UserDTO> temporaryUsers = new ConcurrentHashMap<>();

    @Override
    public void saveTemporaryUser(String email, UserDTO userDTO) {
        temporaryUsers.put(email, userDTO);
    }

    @Override
    public UserDTO getTemporaryUser(String email) {
        return temporaryUsers.get(email);
    }

    @Override
    public void removeTemporaryUser(String email) {
        temporaryUsers.remove(email);
    }
}
