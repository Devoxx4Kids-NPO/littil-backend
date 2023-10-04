package org.littil.api.auth.provider.auth0;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

@QuarkusTest
@QuarkusTestResource(APIManagementMock.class)
class Auth0ManagementTokenProviderTest {

    @Test
    void produceManagementAPI() {
    }
}