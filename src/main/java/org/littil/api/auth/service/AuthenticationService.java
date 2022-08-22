package org.littil.api.auth.service;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationService {

    Optional<AuthUser> getUserById(String userId);

    AuthUser createUser(AuthUser user, String tempPassword);

    void deleteUser(UUID littilUserId);

    void addAuthorization(UUID littilUserId, AuthorizationType type, UUID id);

    void removeAuthorization(UUID littilUserId, AuthorizationType type, UUID id);

}
