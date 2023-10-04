package org.littil.api.auth.provider.auth0;

import com.auth0.exception.Auth0Exception;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.provider.auth0.service.Auth0ManagementTokenProvider;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementTokenProviderTest {

    @Inject
    Auth0ManagementTokenProvider provider;

    @Test
    void getToken() throws Auth0Exception {
        var accessToken = provider.getToken();
        assertNotNull(accessToken);
    }
}