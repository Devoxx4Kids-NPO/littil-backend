package org.littil.api.auth.provider.auth0.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.littil.mock.auth0.APIManagementMock;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RequiredArgsConstructor
@QuarkusTestResource(APIManagementMock.class)
class ManagementAPISupplierTest {

    private final ManagementAPISupplier apiSupplier;
    @Test
    void get() {
        var api = this.apiSupplier.get();

        assertNotNull(api);
        assertNotNull(this.apiSupplier.getToken());
        assertTrue(this.apiSupplier.getToken().getExpiresAt().toInstant().isAfter(Instant.now()));

        assertEquals(api,this.apiSupplier.get());
    }
}