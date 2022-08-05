package org.littil.api.user.service;

import org.littil.api.user.repository.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    User toDomain(UserEntity userEntity);

    @InheritInverseConfiguration(name = "toDomain")
    UserEntity toEntity(User user);
}