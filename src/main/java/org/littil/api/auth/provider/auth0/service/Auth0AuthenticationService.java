package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.provider.auth0.Auth0ManagementAPI;
import org.littil.api.auth.provider.auth0.exception.Auth0AuthorizationException;
import org.littil.api.auth.provider.auth0.exception.Auth0DuplicateUserException;
import org.littil.api.auth.provider.auth0.exception.Auth0UserException;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.service.UserService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class Auth0AuthenticationService implements AuthenticationService {

    private final Auth0UserMapper auth0UserMapper;
    private final Auth0ManagementAPI auth0api;
    private final Auth0RoleService roleService;

    // TODO I don't like the dependency of userService
    private final UserService userService;

    @ConfigProperty(name = "org.littil.auth.token.claim.authorizations")
    String authorizationsClaimName;

    private String getAuth0IdFor(UUID littilUserId) {
        Optional<org.littil.api.user.service.User> userById = userService.getUserById(littilUserId);
        if (userById.isPresent()) {
            return userById.get().getProviderId();
        }
        throw new Auth0UserException("Could not find auth0 id for littilUserId " + littilUserId);
    }

    @Override
    public Optional<AuthUser> getUserById(String userId) {
        try {
            User user = auth0api.users().get(userId, null).execute().getBody();
            AuthUser authUser = getAuthUser(user);
            return Optional.of(authUser);
        } catch (APIException exception) {
            if (exception.getStatusCode() == 404) {
                return Optional.empty();
            }
            throw new Auth0UserException("Could not retrieve user for id " + userId, exception);
        } catch (Auth0Exception exception) {
            throw new Auth0UserException("Could not retrieve user for id " + userId, exception);
        }
    }

    @Override
    public AuthUser createUser(AuthUser authUser, String tempPassword) {
        try {
            UsersPage usersForEmail = auth0api.users().list(new UserFilter().withQuery("email:" + authUser.getEmailAddress())).execute().getBody();
            if (!usersForEmail.getItems().isEmpty()) {
                throw new Auth0DuplicateUserException("User already exists for " + authUser.getEmailAddress());
            }
            User user = auth0api.users().create(auth0UserMapper.toProviderEntity(authUser, tempPassword)).execute().getBody();
            return getAuthUser(user);
        } catch (Auth0Exception exception) {
            throw new Auth0UserException("Could not create user for email " + authUser.getEmailAddress(), exception);
        }
    }

    private AuthUser getAuthUser(User user) throws Auth0Exception {
        List<Role> roles = auth0api.users().listRoles(user.getId(), null).execute().getBody().getItems();
        return auth0UserMapper.toDomain(user, roles);
    }

    @Override
    public void deleteUser(UUID littilUserId) {
        String userId = getAuth0IdFor(littilUserId);
        try {
            auth0api.users().delete(userId).execute();
        } catch (Auth0Exception exception) {
            throw new Auth0UserException("Could not remove user for id " + userId, exception);
        }
    }

    @Override
    public void addAuthorization(UUID littilUserId, AuthorizationType type, UUID resourceId) {
        String userId = getAuth0IdFor(littilUserId);
        try {
            manageAuthorization(userId, type, resourceId, AuthorizationAction.ADD);
        } catch (Auth0Exception e) {
            throw new Auth0AuthorizationException("Unable to add the authorization from auth0 for userId " + userId, e);

        }
    }

    @Override
    public void removeAuthorization(UUID littilUserId, AuthorizationType type, UUID resourceId) {
        String userId = getAuth0IdFor(littilUserId);
        try {
            manageAuthorization(userId, type, resourceId, AuthorizationAction.REMOVE);
        } catch (Auth0Exception e) {
            throw new Auth0AuthorizationException("Unable to remove the authorization from auth0 for userId " + littilUserId, e);
        }
    }

    @Override
    public List<AuthUser> getAllUsers() {
        try {
            Map<Role, List<User>> rolesWithUserList = roleService.getRolesWithUserList();
            Map<String, List<Role>> usersWithRolesList = mapToUsersWithRoleList(rolesWithUserList);
            List<User> users = auth0api.users().list(new UserFilter()).execute().getBody().getItems();
            return users.stream() //
                    .map(user -> auth0UserMapper.toDomain(user,
                            usersWithRolesList.getOrDefault(user.getEmail(), new ArrayList<>()))) //
                    .toList();
        } catch(Auth0Exception exception) {
            throw new Auth0UserException("Could not get list of authUsers", exception);
        }
    }

    private static Map<String, List<Role>> mapToUsersWithRoleList(Map<Role, List<User>> roleWithUserListMap) {
        Map<String,List<Role>>  userWithRolesMap = new HashMap<>();
        for (Map.Entry<Role, List<User>> entry : roleWithUserListMap.entrySet()) {
            Role role = entry.getKey();
            for (User user :  entry.getValue()) {
                userWithRolesMap.computeIfAbsent(user.getEmail(), k -> new ArrayList<>()).add(role);
            }
        }
        return userWithRolesMap;
    }

    private void manageAuthorization(String userId, AuthorizationType type, UUID resourceId, AuthorizationAction action) throws Auth0Exception {
        Map<String, Object> appMetadata = getAppMetadata(userId);
        Map<String, List<String>> authorizations = (Map<String, List<String>>) appMetadata.getOrDefault(authorizationsClaimName, new HashMap<>());
        List<String> authorizationTypeAuthorizations = authorizations.getOrDefault(type.getTokenValue(), new ArrayList<>());

        String roleId = roleService.getIdForRoleName(type.name().toLowerCase());

        // todo can we do this neater? dont like the multiline
        switch (action) {
            case ADD -> {
                auth0api.roles().assignUsers(roleId, List.of(userId)).execute(); // list of users is added, this is not "current state"
                if (!authorizationTypeAuthorizations.contains(resourceId.toString())) {
                    authorizationTypeAuthorizations.add(resourceId.toString());
                }
            }
            case REMOVE -> {
                auth0api.users().removeRoles(userId, List.of(roleId)).execute();
                if (!authorizationTypeAuthorizations.contains(resourceId.toString())) {
                    log.info(String.format("No need to remove authorisation for type %s with id %s because this user does not have any authorizations", type.getTokenValue(), resourceId));
                    return;
                } else {
                    authorizationTypeAuthorizations.remove(resourceId.toString());
                }
            }
        }

        authorizations.put(type.getTokenValue(), authorizationTypeAuthorizations);
        appMetadata.put(authorizationsClaimName, authorizations);

        // we need to create a new user, to prevent an error of editing additional properties.
        User user = getUserWithNewAppMetaData(appMetadata);
        auth0api.users().update(userId, user).execute();
    }

    private Map<String, Object> getAppMetadata(String userId) throws Auth0Exception {
        User userFromAuth = getUserForId(userId);
        return userFromAuth.getAppMetadata();
    }

    private User getUserWithNewAppMetaData(Map<String, Object> appMetadata) {
        // we need to create a new user, to prevent an error of editing additional properties.
        User user = new User();
        user.setAppMetadata(appMetadata);
        return user;
    }

    private User getUserForId(String userId) throws Auth0Exception {
        return auth0api.users().get(userId, null).execute().getBody();
    }

    enum AuthorizationAction {
        ADD,
        REMOVE
    }
}
