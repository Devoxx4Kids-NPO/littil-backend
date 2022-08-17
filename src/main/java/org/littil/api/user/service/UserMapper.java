package org.littil.api.user.service;

import org.littil.api.auth.service.AuthUser;
import org.littil.api.user.api.UserPostResource;
import org.littil.api.user.repository.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.Map;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;
import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;

@Mapper(componentModel = "cdi")
public abstract class UserMapper {

    abstract User toDomain(UserEntity userEntity);
    public abstract User toDomain(AuthUser userEntity);
    public abstract User toDomain(UserPostResource userPostResource);

    @InheritInverseConfiguration(name = "toDomain")
    public abstract UserEntity toEntity(User user);

    AuthUser toAuthUser(User user) {
        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(user.getEmailAddress());
        authUser.setRoles(user.getRoles());
        authUser.setAppMetadata(Map.of(USER_ID_TOKEN_CLAIM, user.getId(), AUTHORIZATIONS_TOKEN_CLAIM, Collections.emptyMap()));
        return authUser;
    }

    //todo why does this method return a void?
    abstract void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);

    abstract void updateDomainFromEntity(UserEntity entity, @MappingTarget User domain);

    abstract void updateDomainFromAuthUser(AuthUser authUser, @MappingTarget User domain);

    abstract void updateAuthUserFromEntity(UserEntity entity, @MappingTarget AuthUser authUser);

    abstract void updateEntityFromAuthUser(AuthUser authUser, @MappingTarget UserEntity entity);
}