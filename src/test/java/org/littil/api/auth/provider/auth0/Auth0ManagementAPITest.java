package org.littil.api.auth.provider.auth0;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RequiredArgsConstructor
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementAPITest {
    private final Auth0ManagementAPI auth0api;

    @Test
    void getManagementAPI() {
        // on init it starts with tokenExpired
        assertTrue(this.auth0api.tokenIsExpired());

        // ManagementAPI get's one and refreshes token in the process
        assertNotNull(this.auth0api.getManagementAPI());
        // this is public api of Auth0ManagementAPI
        assertNotNull(this.auth0api.users());
        assertNotNull(this.auth0api.roles());

        // token is not expired after this
        assertFalse(this.auth0api.tokenIsExpired());
    }

    @Test
    void audienceInit() {
        assertTrue(this.auth0api.audience.endsWith("/api/v2/"));
    }

    @Test
    void createRequestForToken() {
        var requestForToken = this.auth0api.createRequestForToken();

        assertNotNull(requestForToken);
    }
}