package org.littil.api.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.auth0.json.mgmt.users.User;

/**
 * String userId here refers to Auth0 providerId (org.littil.api.user.service.User#providerId)
 */
public interface AuthenticationService {

    Optional<AuthUser> getUserById(String userId);

    AuthUser createUser(AuthUser user, String tempPassword);

    void deleteUser(String userId);

    void addAuthorization(String userId, AuthorizationType type, UUID id);

    void removeAuthorization(String userId, AuthorizationType type, UUID id);

    List<AuthUser> getAllUsers();

	void changeEmailAddress(String providerId, String newEmailAddress);
}
