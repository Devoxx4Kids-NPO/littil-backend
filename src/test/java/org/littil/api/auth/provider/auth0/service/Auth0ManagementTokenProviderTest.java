package org.littil.api.auth.provider.auth0.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementTokenProviderTest {

    @Inject
    Auth0ManagementTokenProvider provider;

    @Test
    void getToken() {
        var accessToken = provider.getNewToken();
        assertTrue(accessToken.isPresent());
        assertTrue(accessToken.get().getExpiresAt().toInstant().isAfter(Instant.now()));
    }
}