package org.littil.api.user.service;

import org.littil.api.auth.service.AuthUser;
import org.littil.api.user.api.UserPostResource;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.userSetting.repository.UserSettingEntity;
import org.littil.api.userSetting.service.UserSetting;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    User toDomain(UserEntity userEntity);
    User toDomain(AuthUser userEntity);
    User toDomain(UserPostResource userPostResource);

    @InheritInverseConfiguration(name = "toDomain")
    UserEntity toEntity(User user);
    @InheritInverseConfiguration(name = "toDomain")
    AuthUser toAuthUser(User user);

    //todo why does this method return a void?
    abstract void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);

    abstract User updateDomainFromEntity(UserEntity entity, @MappingTarget User domain);

    abstract User updateDomainFromAuthUser(User domain, @MappingTarget AuthUser authUser);

    abstract AuthUser updateAuthUserFromEntity(AuthUser authUser, @MappingTarget User domain);
}