package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.dto.RegistrationDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", constant = "ACTIVE")
    UserAccounts registrationDtoToUserAccounts(RegistrationDTO registrationDTO);
}

