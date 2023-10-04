package org.littil.api.auth.provider.auth0.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.littil.api.auth.provider.auth0.exception.Auth0RoleException;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
@RequiredArgsConstructor
class Auth0RoleServiceTest {

    private final Auth0RoleService roles;
    @ParameterizedTest
    @ValueSource(strings={
            "viewer"
    })
    void getIdForRoleName(String roleName) {
        // roles management API is not mocked; we always get exceptions
        assertThrows(Auth0RoleException.class,() -> roles.getIdForRoleName(roleName));
    }
}