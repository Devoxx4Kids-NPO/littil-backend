package org.littil.api.guestTeacher.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.littil.RandomStringGenerator;
import org.junit.jupiter.api.Test;
import org.littil.TestFactory;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleRepository;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    GuestTeacherModuleRepository moduleRepository;
    @InjectMock
    AuthenticationService authenticationService;
    @InjectMock
    TokenHelper tokenHelper;

    @InjectSpy
    GuestTeacherMapper mapper;

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
    void giveFindAll_thenShouldReturPurgedTeachers() {
        final UUID id = UUID.randomUUID();
        final String firstName = RandomStringGenerator.generate(10);
        final String surname = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String postalCode = RandomStringGenerator.generate(6);
        final String country = RandomStringGenerator.generate(10);

        final GuestTeacherEntity guestTeacherEntity = createGuestTeacherEntity(id, firstName, surname );
        final LocationEntity location = new LocationEntity();
        location.setAddress(address);
        location.setPostalCode(postalCode);
        location.setCountry(country);
        guestTeacherEntity.setLocation(location);

        final List<GuestTeacherEntity> expectedTeacherEntities = List.of(guestTeacherEntity);
        final List<GuestTeacher> expectedGuestTeachers = expectedTeacherEntities.stream().map(mapper::toPurgedDomain).toList();

        doReturn(expectedTeacherEntities).when(repository).listAll();

        List<GuestTeacher> guestTeachers = service.findAll();

        assertThat(guestTeachers).isNotEmpty();

        GuestTeacher guestTeacher = guestTeachers.get(0);

        assertEquals(expectedGuestTeachers, guestTeachers);
        assertNull(guestTeacher.getAddress());
        assertNull(guestTeacher.getPostalCode());
        assertNull(guestTeacher.getLocale());
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
    void givenSaveTeacher_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String prefix = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);

        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setFirstName(firstName);
        guestTeacher.setSurname(surname);
        guestTeacher.setPrefix(prefix);
        guestTeacher.setAddress(address);
        guestTeacher.setPostalCode(postalCode);
        guestTeacher.setLocale(locale);

        final LocationEntity location = new LocationEntity();
        location.setAddress(address);
        location.setPostalCode(postalCode);

        final GuestTeacherEntity entity = new GuestTeacherEntity();
        entity.setId(teacherId);
        entity.setFirstName(firstName);
        entity.setPrefix(prefix);
        entity.setSurname(surname);
        entity.setLocation(location);

        final GuestTeacher expectedGuestTeacher = new GuestTeacher();
        expectedGuestTeacher.setId(entity.getId());
        expectedGuestTeacher.setFirstName(entity.getFirstName());
        expectedGuestTeacher.setPrefix(entity.getPrefix());
        expectedGuestTeacher.setSurname(entity.getSurname());
        expectedGuestTeacher.setAddress(entity.getLocation().getAddress());
        expectedGuestTeacher.setPostalCode(entity.getLocation().getPostalCode());

        User user = TestFactory.createUser();
        final UUID userId = user.getId();

        doReturn(entity).when(mapper).toEntity(guestTeacher);
        doReturn(Optional.of(user)).when(userService).getUserById(userId);
        doReturn(true).when(repository).isPersistent(entity);
        doNothing().when(locationRepository).persist(any(LocationEntity.class));
        doReturn(expectedGuestTeacher).when(mapper).updateDomainFromEntity(any(GuestTeacherEntity.class), any(GuestTeacher.class));

        GuestTeacher savedTeacher = service.saveOrUpdate(guestTeacher, userId);
        assertNotNull(savedTeacher);
        assertEquals(expectedGuestTeacher, savedTeacher);
    }

    @Test
    void givenSaveTeacherUnknownErrorOccurred_thenShouldThrowPersistenceException() {
        final UUID teacherId = UUID.randomUUID();
        User user = TestFactory.createUser();
        final UUID userId = user.getId();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);

        final GuestTeacher guestTeacher = createGuestTeacher(null, firstName, surname, address, postalCode, locale);
        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);

        doReturn(Optional.of(user)).when(userService).getUserById(userId);
        doReturn(entity).when(mapper).toEntity(guestTeacher);
        doNothing().when(locationRepository).persist(entity.getLocation());
        doReturn(false).when(repository).isPersistent(entity);

        assertThrows(PersistenceException.class, () -> service.saveOrUpdate(guestTeacher, userId));
    }
    @Test
    void givenSaveTeacherForUnknownUser_thenShouldThrowServiceException() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);

        final GuestTeacher guestTeacher = createGuestTeacher(null, firstName, surname, address, postalCode, locale);
        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);

        final UUID userId = UUID.randomUUID();

        doReturn(entity).when(mapper).toEntity(guestTeacher);
        doReturn(Optional.empty()).when(userService).getUserById(userId);

        assertThrows(ServiceException.class, () -> service.saveOrUpdate(guestTeacher, UUID.randomUUID()));
    }


    @Test
    void givenDeleteGuestTeacherWithModules_thenShouldDeleteTeacherAndUser() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);

        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);
        final GuestTeacherModuleEntity moduleEntity = new GuestTeacherModuleEntity();
        moduleEntity.setId(UUID.randomUUID());
        moduleEntity.setGuestTeacher(entity);
        entity.setModules(List.of(moduleEntity));

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);
        doReturn(1).when(tokenHelper).getNumberOfAuthorizations();

        service.deleteGuestTeacher(teacherId, userId);

        then(repository).should().delete(entity);
        then(moduleRepository).should().delete(moduleEntity);
        then(moduleRepository).shouldHaveNoMoreInteractions();
        then(authenticationService).shouldHaveNoInteractions();
        then(userService).should().deleteUser(userId);
    }

    @Test
    void givenDeleteGuestTeacherWithOutModules_thenShouldDeleteTeacherAndNotDeleteUser() {
        final UUID teacherId = UUID.randomUUID();
        final User user = TestFactory.createUser();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);

        final GuestTeacherEntity entity = createGuestTeacherEntity(teacherId, firstName, surname);
        entity.setModules(null);

        doReturn(Optional.of(user)).when(userService).getUserById(user.getId());
        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);
        doReturn(2).when(tokenHelper).getNumberOfAuthorizations();

        service.deleteGuestTeacher(teacherId, user.getId());

        then(repository).should().delete(entity);
        then(moduleRepository).shouldHaveNoInteractions();
        then(authenticationService).should().removeAuthorization(user.getProviderId(), AuthorizationType.GUEST_TEACHER, teacherId);
    }

    @Test
    void givenDeleteUnknownGuestTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);

        when(repository.findByIdOptional(teacherId)).thenReturn(Optional.empty());
        verifyNoMoreInteractions(repository);

        assertThrows(NotFoundException.class, () -> service.deleteGuestTeacher(teacherId, userId));
    }

    @Test
    void givenDeleteNullGuestTeacher_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.deleteGuestTeacher(null, null));
    }

    @Test
    void givenUpdateTeacher_thenShouldSuccessfullyUpdateTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String newSurname = RandomStringGenerator.generate(10);
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);

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
    void givenUpdateTeacherWithoutUserId_thenShouldThrowServiceException() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);
        final GuestTeacher guestTeacher = createGuestTeacher(teacherId, firstName, surname, address, postalCode, locale);

        then(repository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.saveOrUpdate(guestTeacher,null));
    }

    @Test
    void givenUpdateUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String surname = RandomStringGenerator.generate(10);
        final String firstName = RandomStringGenerator.generate(10);
        final String address = RandomStringGenerator.generate(10);
        final String locale = RandomStringGenerator.generate(2);
        final String postalCode = RandomStringGenerator.generate(6);

        final GuestTeacher guestTeacher = createGuestTeacher(teacherId, firstName, surname, address, postalCode, locale);

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.saveOrUpdate(guestTeacher, userId));
    }

    @Test
    void givenGetUserIdByTeacherId_thenShouldReturnTeacherId() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        GuestTeacherEntity guestTeacherEntity = new GuestTeacherEntity();
        guestTeacherEntity.setId(teacherId);
        guestTeacherEntity.setUser(userEntity);

        doReturn(Optional.of(guestTeacherEntity)).when(repository).findByIdOptional(teacherId);

        UUID foundTeacherId = service.getUserIdByTeacherId(teacherId);

        assertThat(foundTeacherId).isEqualTo(userId);
    }

    @Test
    void givenGetUserIdByUnknownTeacherId_thenShouldThrowNotFoundException() {
        UUID teacherId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);

        assertThrows(NotFoundException.class, () -> service.getUserIdByTeacherId(teacherId));
    }

    @Test
    void givenGetUnknownUserByTeacherId_thenShouldThrowInternalServerErrorException() {
        final UUID teacherId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        GuestTeacherEntity guestTeacherEntity = new GuestTeacherEntity();
        guestTeacherEntity.setId(teacherId);

        doReturn(Optional.of(guestTeacherEntity)).when(repository).findByIdOptional(teacherId);

        assertThrows(InternalServerErrorException.class, () -> service.getUserIdByTeacherId(teacherId));
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