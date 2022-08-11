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
import lombok.RequiredArgsConstructor;
import org.littil.api.auth.Role;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class Auth0AuthenticationService implements AuthenticationService {

    private final Auth0UserMapper auth0UserMapper;
    //TODO create config mapping
    private final RoleMapper roleMapper;
    @Inject
    private ManagementAPI managementAPI;

    @Override
    public List<AuthUser> listUsers() {
        UserFilter filter = new UserFilter();
        try {
            Request<UsersPage> request = managementAPI.users().list(filter);
            UsersPage response = request.execute();
            return response.getItems().stream().map(auth0UserMapper::toDomain).toList();
        } catch (Auth0Exception exception) {
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public AuthUser getUserById(String userId) {
        try {
            Request<User> request = managementAPI.users().get(userId, null);
            return auth0UserMapper.toDomain(request.execute());
        } catch (APIException exception) {
            if (exception.getStatusCode() == 404) {
                throw new NotFoundException(exception.getMessage());
            }
            throw new AuthenticationException(exception.getMessage());
        } catch (Auth0Exception exception) {
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public AuthUser createUser(AuthUser authUser, String tempPassword) {
        try {
            // todo check if user already exists
            return auth0UserMapper.toDomain(managementAPI.users().create(auth0UserMapper.toProviderEntity(authUser, tempPassword)).execute());
        } catch (Auth0Exception exception) {
            exception.printStackTrace();
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            managementAPI.users().delete(userId).execute();
        } catch (Auth0Exception exception) {
            exception.printStackTrace();
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public List<Role> getRoles() {
        try {
            RolesFilter rolesFilter = new RolesFilter();
            RolesPage response = managementAPI.roles().list(rolesFilter).execute();
            return response.getItems().stream().map(roleMapper::toEntity).toList();
        } catch (Auth0Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    // TODO
    public void assignRole(String userId, String roleName) {
        try {
            List<String> userList = List.of(userId);
            Optional<String> roleId = getRoles().stream().filter(r -> r.role().equals(roleName))
                    .map(Role::id).findFirst();
            if (roleId.isPresent()) {
                managementAPI.roles().assignUsers(roleId.get(), userList).execute();
            } else {
                throw new NotFoundException("Role not found");
            }
        } catch (Auth0Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }
}
