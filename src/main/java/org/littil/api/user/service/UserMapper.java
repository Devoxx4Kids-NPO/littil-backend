package org.littil.api.user.service;

import org.littil.api.auth.service.AuthUser;
import org.littil.api.user.api.UserPostResource;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.userSetting.repository.UserSettingEntity;
import org.littil.api.userSetting.service.UserSetting;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;

import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;

@Mapper(componentModel = "cdi")
public abstract class UserMapper {

    abstract User toDomain(UserEntity userEntity);
    public abstract User toDomain(AuthUser userEntity);
    public abstract User toDomain(UserPostResource userPostResource);

    @InheritInverseConfiguration(name = "toDomain")
    abstract UserEntity toEntity(User user);

    AuthUser toAuthUser(User user) {
        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(user.getEmailAddress());
        authUser.setRoles(user.getRoles());
        authUser.setAppMetadata(Collections.singletonMap(USER_ID_TOKEN_CLAIM, user.getId()));
        return authUser;
    }

    //todo why does this method return a void?
    abstract void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);

    abstract void updateDomainFromEntity(UserEntity entity, @MappingTarget User domain);

    abstract void updateDomainFromAuthUser(AuthUser authUser, @MappingTarget User domain);

    abstract void updateAuthUserFromEntity(UserEntity entity, @MappingTarget AuthUser authUser);

    abstract void updateEntityFromAuthUser(AuthUser authUser, @MappingTarget UserEntity entity);
}