package com.onlineauction.OnlineAuction.mapper.context;

import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MappingContext {
    private final UserRepository userRepository;

    @Autowired
    public MappingContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

