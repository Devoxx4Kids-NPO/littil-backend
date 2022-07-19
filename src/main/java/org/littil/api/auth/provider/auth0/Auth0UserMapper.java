package org.littil.api.auth.provider.auth0;

import com.auth0.json.mgmt.users.User;
import org.littil.api.auth.provider.Provider;
import org.mapstruct.Mapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
@Mapper(componentModel = "cdi")
public class Auth0UserMapper {

	User toProviderEntity(org.littil.api.auth.User littleUser) {
		User auth0User = new User("Username-Password-Authentication");
		auth0User.setEmail(littleUser.getEmailAddress());
		auth0User.setPassword(UUID.randomUUID().toString());
		return auth0User;
	}
}
