package org.littil.api.user.api;

import org.littil.api.auth.service.AuthUser;
import org.littil.api.user.service.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public abstract class UserDTOMapper {

    abstract UserDTO toDTO(User user);

    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "loginsCount", source = "loginsCount")
    @Mapping(target = "lastLogin", source = "lastLogin")
    abstract UserDTO updateUserDTOFromAuthUser(AuthUser authUser, @MappingTarget UserDTO user);

}