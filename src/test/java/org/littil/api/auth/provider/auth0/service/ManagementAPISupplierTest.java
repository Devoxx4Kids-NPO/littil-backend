package org.littil.api.auth.provider.auth0.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RequiredArgsConstructor
@QuarkusTestResource(APIManagementMock.class)
class ManagementAPISupplierTest {

    private final ManagementAPISupplier apiSupplier;

    @Test
    void users() {
        var users = this.apiSupplier.users();

        assertNotNull(users);
        assertFalse(this.apiSupplier.tokenIsExpired());
    }

    @Test
    void roles() {
        assertNotNull(this.apiSupplier.roles());
    }
}