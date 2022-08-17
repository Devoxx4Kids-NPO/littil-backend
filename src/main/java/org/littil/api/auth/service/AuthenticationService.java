package org.littil.api.auth.service;

import org.littil.api.auth.Role;

import java.util.List;
import java.util.UUID;

public interface AuthenticationService {
    List<AuthUser> listUsers();

    AuthUser getUserById(String userId);

    AuthUser createUser(AuthUser user, String tempPassword);

    void deleteUser(String userId);

    List<Role> getRoles();

    void addAuthorization(String issuer, AuthorizationType type, UUID id);

    void deleteAuthorization(String issuer, AuthorizationType type, UUID id);

}
