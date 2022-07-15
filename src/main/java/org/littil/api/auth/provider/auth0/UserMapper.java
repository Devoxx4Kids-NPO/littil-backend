package org.littil.api.auth.provider.auth0;

import com.auth0.json.mgmt.users.User;
import org.littil.api.auth.provider.Provider;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
//TODO implement MapStruct
public class UserMapper {

	User toDomain(org.littil.api.auth.User littleUser) {
		User auth0User = new User("Username-Password-Authentication");
		auth0User.setEmail(littleUser.getEmailAddress());
		auth0User.setPassword(UUID.randomUUID().toString());
		return auth0User;
	};

	org.littil.api.auth.User toEntity(User auth0User) {
		return new org.littil.api.auth.User(auth0User.getId(), auth0User.getEmail(), null, Provider.AUTH0, null, null); // TODO
	}

}
