package org.littil.api.auth.provider.auth0;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementAPITest {

    @Inject
    Auth0ManagementAPI auth0ManagementAPI;

    @Test
    void getManagementAPITest() {
        ManagementAPI managementAPI = null;
        try {
            managementAPI = auth0ManagementAPI.getManagementAPI();
        } catch (Auth0Exception exception) {
            fail("unexpected exception", exception);
        }
        assertNotNull(managementAPI);
    }
}