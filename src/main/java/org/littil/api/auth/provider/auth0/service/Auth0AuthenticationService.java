package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Request;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.Role;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.*;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;

@ApplicationScoped
@AllArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class Auth0AuthenticationService implements AuthenticationService {

    private final Auth0UserMapper auth0UserMapper;
    private final RoleMapper roleMapper;
    ManagementAPI managementAPI;

    @Override
    public List<AuthUser> listUsers() {
        UserFilter filter = new UserFilter();
        try {
            Request<UsersPage> request = managementAPI.users().list(filter);
            UsersPage response = request.execute();
            return response.getItems().stream().map(auth0UserMapper::toDomain).toList();
        } catch (Auth0Exception exception) {
            //todo proper exception handling
            throw new AuthenticationException("todo fixme", exception);
        }
    }

    @Override
    public AuthUser getUserById(String userId) {
        try {
            Request<User> request = managementAPI.users().get(userId, null);
            return auth0UserMapper.toDomain(request.execute());
            //todo handle Exception properly
        } catch (APIException exception) {
            if (exception.getStatusCode() == 404) {
                throw new NotFoundException(exception.getMessage());
            }
            throw new AuthenticationException("todo fixme", exception);
        } catch (Auth0Exception exception) {
            throw new AuthenticationException("todo fixme", exception);
        }
    }

    @Override
    public AuthUser createUser(AuthUser authUser, String tempPassword) {
        try {

            // todo check if user already exists
            return auth0UserMapper.toDomain(managementAPI.users().create(auth0UserMapper.toProviderEntity(authUser, tempPassword)).execute());
        } catch (Auth0Exception exception) {
            //todo handle Exception properly
            exception.printStackTrace();
            throw new AuthenticationException("todo fixme", exception);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            managementAPI.users().delete(userId).execute();
        } catch (Auth0Exception exception) {
            //todo handle Exception properly
            exception.printStackTrace();
            throw new AuthenticationException("todo fixme", exception);
        }
    }

    @Override
    public List<Role> getRoles() {
        try {
            RolesFilter rolesFilter = new RolesFilter();
            RolesPage response = managementAPI.roles().list(rolesFilter).execute();
            return response.getItems().stream().map(roleMapper::toEntity).toList();
        } catch (Auth0Exception e) {
            throw new AuthenticationException("todo fixme", e);
        }
    }

    @Override
    //todo refactor single point of definition
    public void addAuthorization(String userId, AuthorizationType type, UUID id) {
        try {
            User userFromAuth = managementAPI.users().get(userId, null).execute();
            // we need to create a new user, to prevent an error of editing additional properties.
            User user = new User();

            Map<String, Object> appMetadata = userFromAuth.getAppMetadata();
            Map<String, List<UUID>> authorizations = (Map<String, List<UUID>>)  appMetadata.getOrDefault(AUTHORIZATIONS_TOKEN_CLAIM, new HashMap<>());
            List<UUID> authorizationTypeAuthorizations = authorizations.getOrDefault(type.getTokenValue(), new ArrayList<>());

            if (!authorizationTypeAuthorizations.contains(id)) {
                authorizationTypeAuthorizations.add(id);
            }
            authorizations.put(type.getTokenValue(), authorizationTypeAuthorizations);
            appMetadata.put(AUTHORIZATIONS_TOKEN_CLAIM, authorizations);

            user.setAppMetadata(appMetadata);
            // todo add role to user
            // todo refactor! Should cache the role? Shouldn't change that much
            RolesPage roles = managementAPI.roles().list(new RolesFilter().withName(type.getTokenValue())).execute();
            String roleId = roles.getItems().stream().findFirst().get().getId();

            managementAPI.users().update(userFromAuth.getId(), user).execute();
            // list of users is added, this is not "current state"
            managementAPI.roles().assignUsers(roleId, List.of(userFromAuth.getId()));
        } catch (Auth0Exception e) {
            //todo better exception handling
            throw new RuntimeException(e);
        }
    }

    @Override
    //todo refactor single point of definition
    public void deleteAuthorization(String issuer, AuthorizationType type, UUID id) {
        try {
            User user = managementAPI.users().get(issuer, null).execute();
            Map<String, Object> appMetadata = user.getAppMetadata();
            Map<String, List<UUID>> authorizations = (Map<String, List<UUID>>)  appMetadata.getOrDefault(AUTHORIZATIONS_TOKEN_CLAIM, new HashMap<>());
            List<UUID> authorizationTypeAuthorizations = authorizations.getOrDefault(type.getTokenValue(), new ArrayList<>());

            if (!authorizationTypeAuthorizations.contains(id)) {
                log.info(String.format("No need to remove authorisation for type %s with id %s because this user does not have any authorizations", type.getTokenValue(), id.toString()));
                return;
            }

            authorizationTypeAuthorizations.remove(id);
            authorizations.put(type.getTokenValue(), authorizationTypeAuthorizations);
            appMetadata.put(AUTHORIZATIONS_TOKEN_CLAIM, authorizations);
            user.setAppMetadata(appMetadata);

            // todo refactor! Should cache the role? Shouldn't change that much
            RolesPage roles = managementAPI.roles().list(new RolesFilter().withName(type.getTokenValue())).execute();
            String roleId = roles.getItems().stream().findFirst().get().getId();
            // todo how to remove users? we can only assign
//            managementAPI.roles().assignUsers(roleId, List.of(userFromAuth.getId()));

            managementAPI.users().update(issuer, user).execute();
        } catch (Auth0Exception e) {
            //todo better exception handling
            throw new RuntimeException(e);
        }
    }
}
