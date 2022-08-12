package org.littil.api.auth.service;

import org.littil.api.auth.Role;

import java.util.List;

public interface AuthenticationService {
    List<AuthUser> listUsers();

    AuthUser getUserById(String userId);

    AuthUser createUser(AuthUser user, String tempPassword);

    void deleteUser(String userId);

    List<Role> getRoles();
}
