package org.littil.api.auth.provider.auth0.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementTokenProviderTest {

    @Inject
    Auth0ManagementTokenProvider provider;


    @Test
    void getToken() {
        var accessToken = provider.getNewToken();
        
        assertNotNull(accessToken);
        assertTrue(accessToken.getExpiresAt().toInstant().isAfter(Instant.now()));
    }

    @Test
    void getAudienceFromOidcTenantConfig() {
        var audience = provider.getAudienceFromOidcTenantConfig();

        assertTrue(audience.isPresent());
        assertTrue(audience.get().endsWith("/api/v2/"));
    }
}