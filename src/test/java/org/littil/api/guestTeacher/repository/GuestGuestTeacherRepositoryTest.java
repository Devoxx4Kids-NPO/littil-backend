package org.littil.api.guestTeacher.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import org.apache.commons.lang3.RandomStringUtils;

import org.junit.jupiter.api.Test;

import org.littil.api.auth.provider.Provider;
import org.littil.api.coordinates.service.Coordinates;
import org.littil.api.coordinates.service.CoordinatesService;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class GuestGuestTeacherRepositoryTest {

    @InjectSpy
    GuestTeacherRepository repository;

    @Inject
    UserRepository userRepository;

    @Inject
    LocationRepository locationRepository;

    @InjectMock
    CoordinatesService coordinatesService;

    @Test
    void givenFindExistingTeacherByName_thenShouldReturnSuccessfully() {
        String firstName = RandomStringUtils.randomAlphabetic(10);
        String surname = RandomStringUtils.randomAlphabetic(10);
        List<GuestTeacherEntity> teacherList = List.of(new GuestTeacherEntity(UUID.randomUUID(), firstName, surname, null, null, null, null));

        doReturn(teacherList).when(repository).list("surname like ?1", "%" + surname + "%");

        List<GuestTeacherEntity> foundTeacher = repository.findBySurnameLike(surname);

        assertThat(teacherList).isEqualTo(foundTeacher);
    }

    @Test
    void givenFindNonExistingTeacherByName_thenShouldReturnEmptyOptional() {
        final String searchSurname = RandomStringUtils.randomAlphabetic(10);

        doReturn(Collections.emptyList()).when(repository).list("surname like ?1", "%" + searchSurname + "%");

        List<GuestTeacherEntity> foundTeacher = repository.findBySurnameLike(searchSurname);

        assertThat(foundTeacher).isEmpty();
    }

    @Test
    @TestSecurity(user = "littil", roles = "viewer")
    @OidcSecurity(claims = {
            @Claim(key = "https://littil.org/littil_user_id", value = "0ea41f01-cead-4309-871c-c029c1fe19bf")
    })
    @TestTransaction
    void givenFindPersistedTeacherByName_thenShouldReturnSuccessfully() {
        String surname = RandomStringUtils.randomAlphabetic(10);
        String address = RandomStringUtils.randomAlphabetic(10);
        String postalCode = RandomStringUtils.randomAlphabetic(6);

        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(surname);

        UserEntity userEntity = createUserEntity();
        userRepository.persist(userEntity);

        doReturn(Coordinates.builder().lat(52.3).lon(4.9).build())
                .when(coordinatesService).getCoordinates(postalCode, address);

        LocationEntity location = createLocation(address, postalCode);
        locationRepository.persist(location);

        guestTeacher.setLocation(location);
        guestTeacher.setUser(userEntity);
        repository.persist(guestTeacher);

        List<GuestTeacherEntity> foundTeacher = repository.findBySurnameLike(surname);

        List<GuestTeacherEntity> expectedTeacher = List.of(guestTeacher);

        assertThat(expectedTeacher).isEqualTo(foundTeacher);
    }

    private UserEntity createUserEntity() {
        return UserEntity.builder()
                .emailAddress(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"))
                .provider(Provider.AUTH0)
                .providerId("0ea41f01-cead-4309-871c-c029c1fe19bf")
                .build();
    }

    private GuestTeacherEntity createGuestTeacherEntity(String surname) {
        return GuestTeacherEntity.builder()
                .firstName(RandomStringUtils.randomAlphabetic(10))
                .prefix(RandomStringUtils.randomAlphabetic(10))
                .surname(surname)
                .build();

    }

    private LocationEntity createLocation(String address, String postalCode) {
        LocationEntity location = new LocationEntity();
        location.setAddress(address);
        location.setPostalCode(postalCode);
        return location;
    }
}