package org.littil.api.guestTeacher.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@QuarkusTest
class GuestTeacherServiceTest {

    @Inject
    GuestTeacherService service;

    @InjectMock
    UserService userService;

    @InjectMock
    LocationRepository locationRepository;

    @InjectMock
    GuestTeacherRepository repository;
    @InjectMock
    AuthenticationService authenticationService;

    @InjectMock
    GuestTeacherMapper mapper;

    @Test
    void givenGetTeacherByName_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final GuestTeacherEntity expectedTeacher = new GuestTeacherEntity();
        expectedTeacher.setId(teacherId);
        expectedTeacher.setSurname(surname);
        final GuestTeacher mappedGuestTeacher = new GuestTeacher();
        mappedGuestTeacher.setId(teacherId);
        mappedGuestTeacher.setSurname(surname);

        doReturn(List.of(expectedTeacher)).when(repository).findBySurnameLike(surname);
        doReturn(mappedGuestTeacher).when(mapper).toDomain(expectedTeacher);

        List<GuestTeacher> guestTeacher = service.getTeacherByName(surname);

        assertEquals(List.of(mappedGuestTeacher), guestTeacher);
    }

    @Test
    void givenGetTeacherByNullName_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getTeacherByName(null));
    }

    @Test
    void givenGetTeacherByUnknownName_thenShouldReturnEmptyList() {
        final String unknownName = RandomStringUtils.randomAlphabetic(10);

        doReturn(Collections.emptyList()).when(repository).findBySurnameLike(unknownName);

        final List<GuestTeacher> guestTeacher = service.getTeacherByName(unknownName);

        assertTrue(guestTeacher.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void givenGetTeacherById_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final GuestTeacherEntity expectedTeacher = new GuestTeacherEntity();
        expectedTeacher.setId(teacherId);
        final GuestTeacher mappedGuestTeacher = new GuestTeacher();
        mappedGuestTeacher.setId(teacherId);

        doReturn(Optional.of(expectedTeacher)).when(repository).findByIdOptional(teacherId);
        doReturn(mappedGuestTeacher).when(mapper).toDomain(expectedTeacher);

        Optional<GuestTeacher> teacher = service.getTeacherById(teacherId);

        assertEquals(Optional.of(mappedGuestTeacher), teacher);
    }

    @Test
    void givenGetTeacherByNullId_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getTeacherById(null));
    }

    @Test
    void givenGetTeacherByUnknownId_thenShouldReturnEmptyOptional() {
        final UUID unknownId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(unknownId);

        final Optional<GuestTeacher> teacher = service.getTeacherById(unknownId);

        assertInstanceOf(Optional.class, teacher);
        assertTrue(teacher.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void giveFindAll_thenShouldReturnTeacherList() {
        final List<GuestTeacherEntity> expectedTeacherEntities = Collections.nCopies(3, new GuestTeacherEntity());
        final List<GuestTeacher> expectedGuestTeachers = expectedTeacherEntities.stream().map(mapper::toDomain).toList();

        doReturn(expectedTeacherEntities).when(repository).listAll();

        List<GuestTeacher> guestTeachers = service.findAll();

        assertEquals(3, guestTeachers.size());
        assertEquals(expectedGuestTeachers, guestTeachers);
    }

    @Test
    void giveFindAll_thenShouldReturnEmptyList() {
        final List<GuestTeacherEntity> expectedTeacherEntities = Collections.emptyList();

        doReturn(expectedTeacherEntities).when(repository).listAll();

        List<GuestTeacher> guestTeachers = service.findAll();

        assertThat(guestTeachers).isEmpty();
        then(mapper).shouldHaveNoInteractions();
    }

    @Test
    void givenSaveTeacherUnknownErrorOccurred_thenShouldThrowPersistenceException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = createGuestTeacher(null, firstName, surname, address, postalCode, locale);
        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);

        final GuestTeacher expectedGuestTeacher = new GuestTeacher();
        expectedGuestTeacher.setId(entity.getId());
        expectedGuestTeacher.setSurname(entity.getSurname());
        expectedGuestTeacher.setFirstName(entity.getFirstName());

        doReturn(Optional.of(new User())).when(userService).getUserById(userId);
        doReturn(entity).when(mapper).toEntity(guestTeacher);
        doNothing().when(locationRepository).persist(entity.getLocation());
        doReturn(false).when(repository).isPersistent(entity);

        assertThrows(PersistenceException.class, () -> service.saveOrUpdate(guestTeacher, userId));
    }


    @Test
    void givenDeleteTeacher_thenShouldDeleteTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);

        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setId(teacherId);
        guestTeacher.setSurname(surname);
        guestTeacher.setFirstName(firstName);

        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);

        service.deleteTeacher(teacherId, userId);

        then(repository).should().delete(entity);
        then(authenticationService).should().removeAuthorization(userId, AuthorizationType.GUEST_TEACHER, teacherId);
    }

    @Test
    void givenDeleteUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);

        when(repository.findByIdOptional(teacherId)).thenReturn(Optional.empty());
        verifyNoMoreInteractions(repository);

        assertThrows(NotFoundException.class, () -> service.deleteTeacher(teacherId, userId));
    }

    @Test
    void givenUpdateTeacher_thenShouldSuccessfullyUpdateTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String newSurname = RandomStringUtils.randomAlphabetic(10);
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = createGuestTeacher(teacherId, firstName, surname, address, postalCode, locale);

        LocationEntity location = new LocationEntity();
        location.setAddress(address);
        location.setPostalCode(postalCode);

        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);
        entity.setLocation(location);

        final GuestTeacher updatedGuestTeacher = createGuestTeacher(entity.getId(), entity.getFirstName(), newSurname,
                entity.getLocation().getAddress(), entity.getLocation().getPostalCode(), locale);

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);
        doReturn(updatedGuestTeacher).when(mapper).updateDomainFromEntity(entity, guestTeacher);

        GuestTeacher persisted = service.saveOrUpdate(guestTeacher, userId);

        then(mapper).should().updateEntityFromDomain(guestTeacher, entity);
        then(repository).should().persist(entity);
        assertThat(persisted.getId()).isEqualTo(teacherId);
        assertThat(persisted.getSurname()).isEqualTo(newSurname);
        assertThat(persisted).usingRecursiveComparison()
                .ignoringFields("surname")
                .isEqualTo(guestTeacher);
    }

    @Test
    void givenUpdateUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = createGuestTeacher(teacherId, firstName, surname, address, postalCode, locale);

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.saveOrUpdate(guestTeacher, userId));
    }

    private GuestTeacher createGuestTeacher(UUID id, String firstName, String surname, String address, String postalCode, String locale) {
        final var guestTeacher = new GuestTeacher();
        guestTeacher.setId(id);
        guestTeacher.setFirstName(firstName);
        guestTeacher.setSurname(surname);
        guestTeacher.setAddress(address);
        guestTeacher.setPostalCode(postalCode);
        guestTeacher.setLocale(locale);
        return guestTeacher;
    }

    private GuestTeacherEntity createGuestTeacherEntity(UUID id, String firstName, String surname) {
        final var guestTeacher = new GuestTeacherEntity();
        guestTeacher.setId(id);
        guestTeacher.setFirstName(firstName);
        guestTeacher.setSurname(surname);
        return guestTeacher;
    }
}