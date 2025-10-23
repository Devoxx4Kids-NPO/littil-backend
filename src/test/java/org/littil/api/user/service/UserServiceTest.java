package org.littil.api.user.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserServiceTest {

    @InjectSpy
    UserService service;

    @Test
    @Transactional
    void testGetUserStatistics() {
        // Arrange
        UUID id = UUID.randomUUID();
        String auth0id = "auth0id";
        String email = "email@littil.org";

        // Act
        service.createAndPersistDevData(id, auth0id, email);

        // Assert
        Optional<User> user = service.getUserById(id);
        assertTrue(user.isPresent());
    }

}
