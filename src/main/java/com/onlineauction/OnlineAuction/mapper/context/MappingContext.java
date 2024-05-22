package com.onlineauction.OnlineAuction.mapper.context;

import com.onlineauction.OnlineAuction.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class MappingContext {
    private final UserRepository userRepository;
}

