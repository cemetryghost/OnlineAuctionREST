package com.onlineauction.OnlineAuction.mapper;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", source = "role")
    UserAccounts userDTOToUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    UserDTO userToUserDTO(UserAccounts user);

    @Mapping(target = "password", ignore = true)
    void updateUserFromDto(UserDTO dto, @MappingTarget UserAccounts entity);
}
