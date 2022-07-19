package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.AuthRequest;
import com.auth0.net.Request;
import io.quarkus.oidc.OidcTenantConfig;
import lombok.RequiredArgsConstructor;
import org.littil.api.auth.Role;
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
    OidcTenantConfig tenantConfig;

    @Override
    public List<org.littil.api.auth.User> listUsers() {
        UserFilter filter = new UserFilter();
        try {
            ManagementAPI mgmt = getMgmtApi();
            Request<UsersPage> request = mgmt.users().list(filter);
            UsersPage response = request.execute();
            return response.getItems().stream().map(auth0UserMapper::toDomain).toList();
        } catch (Auth0Exception exception) {
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public org.littil.api.auth.User getUserById(String userId) {
        try {
            ManagementAPI mgmt = getMgmtApi();
            Request<User> request = mgmt.users().get(userId, null);
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

    public org.littil.api.auth.User createUser(org.littil.api.auth.User authUser) {
        try {
            ManagementAPI mgmt = getMgmtApi();
            return auth0UserMapper.toDomain(mgmt.users().create(auth0UserMapper.toProviderEntity(authUser)).execute());
        } catch (Auth0Exception exception) {
            exception.printStackTrace();
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            ManagementAPI mgmt = getMgmtApi();
            mgmt.users().delete(userId).execute();
        } catch (Auth0Exception exception) {
            exception.printStackTrace();
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public org.littil.api.auth.User getUserByEmail(String email) {
        try {
            ManagementAPI mgmt = getMgmtApi();
            UserFilter filter = new UserFilter();
            Request<List<User>> request = mgmt.users().listByEmail(email, filter);
            List<User> response = request.execute();
            return response.stream().map(auth0UserMapper::toDomain).findFirst().get();
            // TODO what if not found - findFirst=Optional
        } catch (Auth0Exception exception) {
            throw new AuthenticationException(exception.getMessage());
        }
    }

    @Override
    public List<Role> getRoles() {
        try {
            ManagementAPI mgmt = getMgmtApi();
            RolesFilter rolesFilter = new RolesFilter();
            RolesPage response = mgmt.roles().list(rolesFilter).execute();
            return response.getItems().stream().map(roleMapper::toEntity).toList();
        } catch (Auth0Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    // TODO
    public void assignRole(String userId, String roleName) {
        try {
            ManagementAPI mgmt = getMgmtApi();
            List<String> userList = List.of(userId);
            Optional<String> roleId = getRoles().stream().filter(r -> r.role().equals(roleName))
                    .map(Role::id).findFirst();
            if (roleId.isPresent()) {
                mgmt.roles().assignUsers(roleId.get(), userList).execute();
            } else {
                throw new NotFoundException("Role not found");
            }
        } catch (Auth0Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    private ManagementAPI getMgmtApi() throws Auth0Exception {
        AuthAPI authAPI = new AuthAPI(tenantConfig.getTenantId().map(Object::toString).orElse(""), tenantConfig.getClientId().map(Object::toString).orElse(""), tenantConfig.getCredentials().getClientSecret().toString());
        AuthRequest authRequest = authAPI.requestToken(tenantConfig.getAuthorizationPath());

        TokenHolder holder = authRequest.execute();
        return new ManagementAPI(DOMAIN, holder.getAccessToken());
    }

}
