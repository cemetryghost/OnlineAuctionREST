package com.onlineauction.OnlineAuction.mapper;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "email", source = "email")
    UserAccounts userDTOToUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", source = "email")
    UserDTO userToUserDTO(UserAccounts user);

    @Mapping(target = "password", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget UserAccounts entity);
}
