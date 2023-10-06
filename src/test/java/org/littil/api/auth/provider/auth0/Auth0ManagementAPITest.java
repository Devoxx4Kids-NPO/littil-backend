package org.littil.api.auth.provider.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RequiredArgsConstructor
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementAPITest {
    private final Auth0ManagementAPI auth0ManagementAPI;

    @Test
    void getManagementAPI() throws Auth0Exception {
        // on init it starts with tokenExpired
        assertTrue(auth0ManagementAPI.tokenIsExpired());

        // ManagementAPI get's one and refreshes token in the process
        assertNotNull(auth0ManagementAPI.getManagementAPI());

        // token is not expired after this
        assertFalse(auth0ManagementAPI.tokenIsExpired());
    }

    @Test
    void audienceInit() {
        assertTrue(auth0ManagementAPI.audience.endsWith("/api/v2/"));
    }
}