package org.littil.api.auth.service;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationService {

    Optional<AuthUser> getUserById(String userId);

    AuthUser createUser(AuthUser user, String tempPassword);

    void deleteUser(String userId);

    void addAuthorization(String issuer, AuthorizationType type, UUID id);

    void removeAuthorization(String issuer, AuthorizationType type, UUID id);

}
