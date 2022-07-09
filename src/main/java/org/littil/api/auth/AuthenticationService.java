package org.littil.api.auth;

import java.util.List;

public interface AuthenticationService {

	public List<User> listUsers();

	public User getUserById(String userId);

	public User createUser(User user);

	public User getUserByEmail(String email);

	public void deleteUser(String userId);

	public List<RoleEntity> getRoles();

}
