package org.littil.api.auth.provider.auth0.service;

import static org.mockito.Mockito.*;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.roles.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Request;
import com.auth0.net.Response;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.provider.auth0.Auth0ManagementAPI;
import org.littil.api.auth.provider.auth0.exception.Auth0RoleException;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@SuppressWarnings("unchecked")
public class Auth0RoleServiceTest {

    private Auth0ManagementAPI auth0api;
    private Response<RolesPage> response;

    Auth0RoleService roleService;

    @BeforeEach
    public void setUp() {
        auth0api = mock(Auth0ManagementAPI.class);
        response = mock(Response.class);

        // Ensure that roles is properly mocked
        RolesEntity rolesEntity = mock(RolesEntity.class);
        when(auth0api.roles()).thenReturn(rolesEntity);
        when(rolesEntity.list(any())).thenReturn(mock(Request.class));

        // initiate roleService with mock Auth0ManagementAPI
        roleService = new Auth0RoleService(auth0api);
    }

    @Test
    public void whenRoleNameExist_thenGetRoleId_returnsRoleId() throws Exception {
        String roleName = "admin";
        String roleDescription = "adminstrator role";
        String roleId = "role123";

        // Set up the mock behavior
        Role role = createRole(roleName, roleDescription, roleId);
        RolesPage rolesPage = new RolesPage(List.of(role));

        when(auth0api.roles().list(any()).execute()).thenReturn(response);
        when(response.getBody()).thenReturn(rolesPage);

        clearInvocations(auth0api);

        // Execute the code under test

        // roleService should get roleId from api
        String receivedRoleId = roleService.getIdForRoleName(roleName);
        assertEquals(roleId, receivedRoleId);
        verify(auth0api,times(1)).roles();

        // roleService should get roleId from cash
        receivedRoleId = roleService.getIdForRoleName(roleName);
        assertEquals(roleId, receivedRoleId);
        verify(auth0api, times(1)).roles();
    }

    @Test
    public void whenRoleNameMissing_thenGetRoleId_returnsAuth0RoleException() throws Auth0Exception {
        String roleName = "missingRole";

        // Set up the mock behavior
        RolesPage rolesPage = new RolesPage(new ArrayList<>());

        when(auth0api.roles().list(any()).execute()).thenReturn(response);
        when(response.getBody()).thenReturn(rolesPage);

        // Execute the code under test
        Auth0RoleException exception = assertThrows(Auth0RoleException.class,
                () -> roleService.getIdForRoleName(roleName));
        assertEquals("Could not find role " + roleName, exception.getMessage());
    }

    @Test
    public void whenAuthOException_thenGetRoleId_returnsAuth0RoleException() throws Auth0Exception {
        String roleName = "admin";

        // Set up the mock behavior
        when(auth0api.roles().list(any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0RoleException exception = assertThrows(Auth0RoleException.class,
                () -> roleService.getIdForRoleName(roleName));
        assertEquals("Could not retrieve role for " + roleName, exception.getMessage());
    }

    @Test
    public void whenRolesExists_thenGetRolesWithUserList_returnMapWithRoles() throws Auth0Exception {

        // Set up the mock behavior
        Role role1 = createRole("role1", "description role1", "1");
        Role role2 = createRole("role2", "description role2", "2");
        RolesPage rolesPage = new RolesPage(List.of(role1, role2));

        when(auth0api.roles().list(any()).execute()).thenReturn(response);
        when(response.getBody()).thenReturn(rolesPage);

        // default no users for any role
        when(auth0api.roles().listUsers(any(), any())).thenReturn(mock(Request.class));
        when(auth0api.roles().listUsers(any(), any()).execute()).thenReturn(mock(Response.class));
        when(auth0api.roles().listUsers(any(), any()).execute().getBody()).thenReturn(new UsersPage(new ArrayList<>()));

        // user1 is assigned to role1
        User user1 = new User();
        user1.setName("user1");

        when(auth0api.roles().listUsers(role1.getId(), null)).thenReturn(mock(Request.class));
        Response<UsersPage> response1 = mock(Response.class);
        when(auth0api.roles().listUsers(role1.getId(),null).execute()).thenReturn(response1);
        when(response1.getBody()).thenReturn(new UsersPage(List.of(user1)));

        // Execute the code under test

        // roleService should get map of rules with user list
        Map<Role, List<User>> roles = roleService.getRolesWithUserList();

        assertNotNull(roles);
        assertEquals(2, roles.size());

        // role 1 should have user1
        assertTrue(roles.containsKey(role1));
        assertNotNull(roles.get(role1));
        List<User> role1UserList = roles.get(role1);
        assertEquals(1, role1UserList.size());
        assertTrue(role1UserList.contains(user1));

        // role2 should have empty list
        assertTrue(roles.containsKey(role2));
        assertTrue(roles.get(role2).isEmpty());
    }

    @Test
    public void whenAuth0Exception_thenGetRolesWithUserList_returnAuth0RoleException() throws Auth0Exception {

        // Set up the mock behavior
        when(auth0api.roles().list(any()).execute()).thenThrow(new Auth0Exception("simulated exception"));

        // Execute the code under test
        Auth0RoleException exception = assertThrows(Auth0RoleException.class,
                () -> roleService.getRolesWithUserList());
        assertEquals("Could not get map with roles", exception.getMessage());
    }

    private Role createRole(String roleName, String roleDescription, String roleId)  {
        Role role = new Role();
        role.setName(roleName);
        role.setDescription(roleDescription);
        try {
            Field field = Role.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(role, roleId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return role;
    }
}