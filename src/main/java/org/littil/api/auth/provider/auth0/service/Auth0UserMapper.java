package org.littil.api.auth.provider.auth0.service;

import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.AuthUser;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public class Auth0UserMapper {
    User toProviderEntity(AuthUser littleUser, String tempPassword) {
        User auth0User = new User("Username-Password-Authentication");
        auth0User.setEmail(littleUser.getEmailAddress());
        auth0User.setPassword(tempPassword.toCharArray());
        auth0User.setEmailVerified(true);
        auth0User.setAppMetadata(littleUser.getAppMetadata());
        return auth0User;
    }

    public AuthUser toDomain(User user, List<Role> roles) {
        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(user.getEmail());
        authUser.setProviderId(user.getId());
        Set<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        authUser.setRoles(roleNames);
        authUser.setProvider(Provider.AUTH0);
        authUser.setLoginsCount(user.getLoginsCount());
        authUser.setLastLogin(user.getLastLogin());
        return authUser;
    }
}
