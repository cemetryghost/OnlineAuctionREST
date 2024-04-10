package com.onlineauction.OnlineAuction.context;

import com.onlineauction.OnlineAuction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingContext {
    private final UserRepository userRepository;

    @Autowired
    public MappingContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}

