package org.littil.api.user.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.api.UserPostResource;
import org.littil.api.user.repository.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import jakarta.inject.Inject;
import java.util.Map;

import static java.util.Collections.emptyList;

@Mapper(componentModel = "cdi")
public abstract class UserMapper {

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.user_id")
    String userIdClaimName;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.authorizations")
    String authorizationsClaimName;

    abstract User toDomain(UserEntity userEntity);

    public abstract User toDomain(AuthUser userEntity);

    public abstract User toDomain(UserPostResource userPostResource);

    @InheritInverseConfiguration(name = "toDomain")
    public abstract UserEntity toEntity(User user);

    AuthUser toAuthUser(User user) {
        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(user.getEmailAddress());
        authUser.setRoles(user.getRoles());
        authUser.setAppMetadata(Map.of(userIdClaimName, user.getId(), authorizationsClaimName, Map.of(AuthorizationType.SCHOOL.getTokenValue(), emptyList(), AuthorizationType.GUEST_TEACHER.getTokenValue(), emptyList())));
        return authUser;
    }

    //todo why does this method return a void?
    abstract void updateEntityFromDomain(User domain, @MappingTarget UserEntity entity);

    abstract void updateDomainFromEntity(UserEntity entity, @MappingTarget User domain);

    abstract void updateDomainFromAuthUser(AuthUser authUser, @MappingTarget User domain);

    abstract void updateAuthUserFromEntity(UserEntity entity, @MappingTarget AuthUser authUser);

    abstract void updateEntityFromAuthUser(AuthUser authUser, @MappingTarget UserEntity entity);
}