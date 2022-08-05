package org.littil.api.auth.service;

import org.littil.api.auth.Role;
import org.littil.api.user.service.User;

import java.util.List;

public interface AuthenticationService {
    List<User> listUsers();

    User getUserById(String userId);

    User createUser(User user);

    User getUserByEmail(String email);

    void deleteUser(String userId);

    List<Role> getRoles();
}
