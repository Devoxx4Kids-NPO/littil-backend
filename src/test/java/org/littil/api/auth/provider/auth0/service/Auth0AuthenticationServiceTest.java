package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.roles.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Request;
import com.auth0.net.Response;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.provider.auth0.Auth0ManagementAPI;
import org.littil.api.auth.provider.auth0.exception.Auth0AuthorizationException;
import org.littil.api.auth.provider.auth0.exception.Auth0DuplicateUserException;
import org.littil.api.auth.provider.auth0.exception.Auth0UserException;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthorizationType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
@SuppressWarnings("unchecked")
class Auth0AuthenticationServiceTest {

    private static final String CLAIM_NAME = "authorizations";
    @Inject
    Auth0UserMapper auth0UserMapper;
    private Auth0ManagementAPI auth0api;
    private Auth0RoleService roleService;

    private Response<User> userResponse;
    private Response<RolesPage> rolesPageResponse;

    Auth0AuthenticationService authenticationService;

    @BeforeEach
    void setUp() throws Auth0Exception {
        auth0api = mock(Auth0ManagementAPI.class);
        roleService = mock(Auth0RoleService.class);
        userResponse = mock(Response.class);
        rolesPageResponse = mock(Response.class);

        // Ensure that users() is properly mocked
        when(auth0api.users()).thenReturn(mock(UsersEntity.class));
        when(auth0api.users().create(any())).thenReturn(mock(Request.class));
        when(auth0api.users().delete(any())).thenReturn(mock(Request.class));
        when(auth0api.users().get(any(),any())).thenReturn(mock(Request.class));
        when(auth0api.users().list(any())).thenReturn(mock(Request.class));
        when(auth0api.users().listRoles(any(),any())).thenReturn(mock(Request.class));
        when(auth0api.users().update(any(), any())).thenReturn(mock(Request.class));
        when(auth0api.users().update(any(), any()).execute()).thenReturn(mock(Response.class));

        // Ensure that roles() is properly mocked
        when(auth0api.roles()).thenReturn(mock(RolesEntity.class));
        when(auth0api.roles().assignUsers(any(), any())).thenReturn(mock(Request.class));
        when(auth0api.roles().assignUsers(any(), any()).execute()).thenReturn(mock(Response.class));
        when(auth0api.users().removeRoles(any(), any())).thenReturn(mock(Request.class));
        when(auth0api.users().removeRoles(any(), any()).execute()).thenReturn(mock(Response.class));

        // initiate authenticationService
        authenticationService = new Auth0AuthenticationService(auth0UserMapper,auth0api, roleService, CLAIM_NAME);
    }

    @Test
    void whenAuthUserExist_thenGetUserById_returnOptionalOfAuthUser() throws Auth0Exception {
        // Set up the mock behavior
        String userId = UUID.randomUUID().toString();
        User user = new User();

        RolesPage rolesPage = new RolesPage(new ArrayList<>());

        when(auth0api.users().get(any(),any()).execute()).thenReturn(userResponse);
        when(userResponse.getBody()).thenReturn(user);

        when(auth0api.users().listRoles(any(),any()).execute()).thenReturn(rolesPageResponse);
        when(rolesPageResponse.getBody()).thenReturn(rolesPage);

        // Execute the code under test
        Optional<AuthUser> authUser = authenticationService.getUserById(userId);
        assertTrue(authUser.isPresent());
    }

    @Test
    void whenAuthUserNotExists_thenGetUserById_returnOptionalEmpty() throws Auth0Exception {
        // Set up the mock behavior
        String userId = UUID.randomUUID().toString();

        when(auth0api.users().get(any(),any()).execute()).thenThrow(new APIException("User not found", 404, null));

        // Execute the code under test
        Optional<AuthUser> authUser = authenticationService.getUserById(userId);
        assertTrue(authUser.isEmpty());
    }

    @Test
    void whenApiException_thenGetUserById_returnAuth0UserException() throws Auth0Exception {
        // Set up the mock behavior
        String userId = UUID.randomUUID().toString();

        when(auth0api.users().get(any(),any()).execute()).thenThrow(new APIException("simulated exception", 500, null));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.getUserById(userId));
        assertEquals("Could not retrieve user for id " + userId, exception.getMessage());
    }

    @Test
    void whenAuth0Exception_thenGetUserById_returnAuth0UserException() throws Auth0Exception {
        // Set up the mock behavior
        String userId = UUID.randomUUID().toString();

        when(auth0api.users().get(any(),any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.getUserById(userId));
        assertEquals("Could not retrieve user for id " + userId, exception.getMessage());
    }

    @Test
    void whenEmailNotExists_thenCreateUser_isSuccessful() throws Auth0Exception {
        // Set up the mock behavior
        AuthUser authUser = new AuthUser();
        String tempPassword = UUID.randomUUID().toString();

        Response<UsersPage> usersPageResponse = mock(Response.class);
        when(auth0api.users().list(any()).execute()).thenReturn(usersPageResponse);

        UsersPage usersPage = new UsersPage(new ArrayList<>());
        when(usersPageResponse.getBody()).thenReturn(usersPage);

        when(auth0api.users().create(any()).execute()).thenReturn(userResponse);

        User user = new User();
        when(userResponse.getBody()).thenReturn(user);

        RolesPage rolesPage = new RolesPage(new ArrayList<>());
        when(auth0api.users().listRoles(any(),any()).execute()).thenReturn(rolesPageResponse);
        when(rolesPageResponse.getBody()).thenReturn(rolesPage);

        // Execute the code under test
        AuthUser saved = authenticationService.createUser(authUser, tempPassword);
        assertNotNull(saved);
    }

    @Test
    void whenEmailExists_thenCreateUser_returnAuth0UserException() throws Auth0Exception {
        // Set up the mock behavior
        String email = "test@littil.org";

        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(email);
        String tempPassword = UUID.randomUUID().toString();

        Response<UsersPage> usersPageResponse = mock(Response.class);
        when(auth0api.users().list(any()).execute()).thenReturn(usersPageResponse);

        User existingUser = new User();
        existingUser.setEmail(email);
        UsersPage usersPage = new UsersPage(List.of(existingUser));
        when(usersPageResponse.getBody()).thenReturn(usersPage);

        // Execute the code under test
        Auth0DuplicateUserException exception = assertThrows(Auth0DuplicateUserException.class,
                () -> authenticationService.createUser(authUser, tempPassword));
        assertEquals("User already exists for " + email, exception.getMessage());
    }

    @Test
    void whenAuth0Exception_thenCreateUser_returnAuth0UserException() throws Auth0Exception {
        // Set up the mock behavior
        String email = "test@littil.org";

        AuthUser authUser = new AuthUser();
        authUser.setEmailAddress(email);
        String tempPassword = UUID.randomUUID().toString();

        when(auth0api.users().list(any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.createUser(authUser, tempPassword));
        assertEquals("Could not create user for email " + email, exception.getMessage());
    }

    @Test
    void whenUserExists_thenDeleteUser_deletesUser() {
        try {
            // Set up the mock behavior
            String providerId = UUID.randomUUID().toString();
            when(auth0api.users().delete(any()).execute()).thenReturn(mock(Response.class));

            // Execute the code under test
            authenticationService.deleteUser(providerId);
        } catch (Auth0Exception exception) {
            fail("Unexpected exception", exception);
        }
    }

    @Test
    void whenAuth0Exception_thenDeleteUser_returnAuth0UserException() throws Auth0Exception {
        // Set up the mock behavior
        String providerId = UUID.randomUUID().toString();
        when(auth0api.users().delete(any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.deleteUser(providerId));
        assertEquals("Could not remove user for id " + providerId, exception.getMessage());
    }

    @Test
    void addAuthorizationTest() throws Auth0Exception {
        // Set up the mock behavior
        String providerId = UUID.randomUUID().toString();
        UUID resourceId = UUID.randomUUID();

        org.littil.api.user.service.User littilUser = new org.littil.api.user.service.User();
        littilUser.setProviderId(providerId);

        User user = new User();
        user.setAppMetadata(new HashMap<>());
        when(auth0api.users().get(any(),any()).execute()).thenReturn(userResponse);
        when(userResponse.getBody()).thenReturn(user);

        when(roleService.getIdForRoleName(any())).thenReturn("roleId");

        // Execute the code under test
        authenticationService.addAuthorization(providerId, AuthorizationType.SCHOOL, resourceId);

        // Verify: auth0api call was invoked
        verify(auth0api.users().get(anyString(), any()))
                .execute();

    }

    @Test
    void whenAuth0Exception_thenAddAuthorization_returnAuth0AuthorizationException() throws Auth0Exception {
        // Set up the mock behavior
        UUID resourceId = UUID.randomUUID();
        String providerId = UUID.randomUUID().toString();

        when(auth0api.users().get(any(),any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0AuthorizationException exception = assertThrows(Auth0AuthorizationException.class, () ->
            authenticationService.addAuthorization(providerId, AuthorizationType.SCHOOL, resourceId));
        assertEquals("Unable to add the authorization from auth0 for userId " + providerId, exception.getMessage());
    }

    @Test
    void removeAuthorizationTest() throws Auth0Exception {
        // Set up the mock behavior
        String providerId = UUID.randomUUID().toString();
        UUID resourceId = UUID.randomUUID();

        User user = new User();
        List<String> authorizationsSchools = new ArrayList<>();
        authorizationsSchools.add(resourceId.toString());
        Map<String, List<String>> authorizations = new HashMap<>();
        authorizations.put("schools", authorizationsSchools);
        Map<String,Object> appMetadata = new HashMap<>();
        appMetadata.put(CLAIM_NAME, authorizations);
        user.setAppMetadata(appMetadata);

        when(auth0api.users().get(any(),any()).execute()).thenReturn(userResponse);
        when(userResponse.getBody()).thenReturn(user);

        when(roleService.getIdForRoleName(any())).thenReturn("roleId");

        // Execute the code under test
        authenticationService.removeAuthorization(providerId, AuthorizationType.SCHOOL, resourceId);
        // nothing to validate because addAuthorzation method is of type void
    }

    @Test
    void whenAuth0Exception_thenRemoveAuthorization_returnAuth0AuthorizationException() throws Auth0Exception {
        // Set up the mock behavior
        UUID resourceId = UUID.randomUUID();
        String providerId = UUID.randomUUID().toString();


        when(auth0api.users().get(any(),any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0AuthorizationException exception = assertThrows(Auth0AuthorizationException.class, () ->
            authenticationService.removeAuthorization(providerId, AuthorizationType.SCHOOL, resourceId));
        assertEquals("Unable to remove the authorization from auth0 for userId " + providerId, exception.getMessage());
    }

    @Test
    void getAllUsersTest() throws Auth0Exception {
        // Set up the mock behavior
        User user1 = new User();
        user1.setEmail("user1@littil.org");
        User user2 = new User();
        user2.setEmail("user2@littil.org");

        Role role1 = new Role();
        role1.setName("role1");

        Role role2 = new Role();
        role2.setName("role2");

        Map<Role, List<User>> rolesMap = new HashMap<>();
        rolesMap.put(role1,List.of(user1));
        rolesMap.put(role2, new ArrayList<>());
        when(roleService.getRolesWithUserList()).thenReturn(rolesMap);

        Response<UsersPage> userPageResponse = mock(Response.class);
        when(auth0api.users().list(any()).execute()).thenReturn(userPageResponse);

        List<User> userList = List.of(user1, user2);
        when(userPageResponse.getBody()).thenReturn(new UsersPage(userList));

        // Execute the code under test
        List<AuthUser> authUserList = authenticationService.getAllUsers();

        assertNotNull(authUserList);
        assertFalse(authUserList.isEmpty());
        assertEquals(2, authUserList.size());

        Optional<AuthUser> authUser1 = authUserList.stream()
                .filter(user -> user.getEmailAddress().equals("user1@littil.org"))
                .findFirst();
        assertTrue(authUser1.isPresent());
        assertFalse(authUser1.get().getRoles().isEmpty());
        assertEquals(1, authUser1.get().getRoles().size());
        assertTrue(authUser1.get().getRoles().contains("role1"));

        Optional<AuthUser> authUser2 = authUserList.stream()
                .filter(user -> user.getEmailAddress().equals("user2@littil.org"))
                .findFirst();
        assertTrue(authUser2.isPresent());
        assertTrue(authUser2.get().getRoles().isEmpty());
    }

    @Test
    void whenAuth0Exception_thenGetAllUsers_returnAuth0userException() throws Auth0Exception {
        // Set up the mock behavior
        when(roleService.getRolesWithUserList()).thenReturn(new HashMap<>());
        when(auth0api.users().list(any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.getAllUsers());
        assertEquals("Could not get list of authUsers" , exception.getMessage());
    }

    @Test
    void changeEmailAddressTest() throws Auth0Exception {

        // Set up the mock behavior
        UsersEntity usersEntity = mock(UsersEntity.class);
        Request<User> updateRequest = mock(Request.class);

        when(auth0api.users()).thenReturn(usersEntity);
        when(usersEntity.update(any(), any())).thenReturn(updateRequest);
        when(updateRequest.execute()).thenReturn(null);

        // Execute the code under test
        authenticationService.changeEmailAddress("providerId", "email@littil.org");

        verify(usersEntity, times(1)).update(any(), any());
        verify(updateRequest, times(1)).execute();
    }

    @Test
    void whenAuth0Exception_thenChangeEmailAddress_returnAuth0userException() throws Auth0Exception {

        // Set up the mock behavior
        when(auth0api.users().update(any(),any()).execute())
                .thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0UserException exception = assertThrows(Auth0UserException.class,
                () -> authenticationService.changeEmailAddress("providerId", "email@littil.org"));
        assertEquals("Could not change email for user with providerId providerId" , exception.getMessage());
    }

}
