package org.littil.api.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.littil.RandomStringGenerator;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.provider.Provider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserRepositoryTest {

    @InjectSpy
    UserRepository repository;

    @Test
    void givenFindExistingUserByEmailAddress_thenShouldReturnSuccessfully() {
        String email = RandomStringGenerator.generate(10) + "@littil.org";
        final Optional<UserEntity> userEntityOptional
                = getDefaultUser(RandomStringGenerator.generate(10), email);

        final PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("emailAddress", email);
        when(query.firstResultOptional()).thenReturn(userEntityOptional);

        final Optional<UserEntity> foundUser = repository.findByEmailAddress(email);

        assertThat(foundUser).isEqualTo(userEntityOptional);
    }

    @Test
    void givenFindNonExistingUserByEmailAddress_thenShouldReturnEmptyOptional() {
        String email = RandomStringGenerator.generate(10) + "@littil.org";

        final PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("emailAddress", email);
        when(query.firstResultOptional()).thenReturn(Optional.empty());

        final Optional<UserEntity> foundUser = repository.findByEmailAddress(email);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void givenFindExistingUserByProviderId_thenShouldReturnSuccessfully() {
        String providerId = RandomStringGenerator.generate(10);

        final Optional<UserEntity> userEntityOptional
                = getDefaultUser(providerId, RandomStringGenerator.generate(10) + "@littil.org");

        final PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("providerId", providerId);
        when(query.firstResultOptional()).thenReturn(userEntityOptional);

        final Optional<UserEntity> foundUser = repository.findByProviderId(providerId);

        assertThat(foundUser).isEqualTo(userEntityOptional);
    }

    @Test
    void givenFindNonExistingUserByProviderId_thenShouldReturnEmptyOptional() {
        String providerId = RandomStringGenerator.generate(10);

        final PanacheQuery<UserEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("providerId", providerId);
        when(query.firstResultOptional()).thenReturn(Optional.empty());

        final Optional<UserEntity> foundUser = repository.findByProviderId(providerId);

        assertThat(foundUser).isEmpty();
    }

    private Optional<UserEntity> getDefaultUser(String providerId, String email) {
        return Optional.of(new UserEntity(UUID.randomUUID(),
                Provider.AUTH0,
                providerId,
                email));
    }
}