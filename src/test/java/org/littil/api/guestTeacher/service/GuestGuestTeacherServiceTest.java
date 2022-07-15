package org.littil.api.guestTeacher.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.Validator;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class GuestGuestTeacherServiceTest {

    @Inject
    GuestTeacherService service;

    @InjectMock
    GuestTeacherRepository repository;

    @InjectMock
    GuestTeacherMapper mapper;

    @Test
    void givenGetTeacherByName_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final GuestTeacherEntity expectedTeacher = GuestTeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .build();
        final GuestTeacher mappedGuestTeacher = new GuestTeacher();
        mappedGuestTeacher.setId(teacherId);
        mappedGuestTeacher.setSurname(surname);

        doReturn(List.of(expectedTeacher)).when(repository).findByName(surname);
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

        doReturn(Collections.emptyList()).when(repository).findByName(unknownName);

        final List<GuestTeacher> guestTeacher = service.getTeacherByName(unknownName);

        assertTrue(guestTeacher.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void givenGetTeacherById_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final GuestTeacherEntity expectedTeacher = GuestTeacherEntity.builder()
                .id(teacherId)
                .build();
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
        final List<GuestTeacherEntity> expectedTeacherEntities = Collections.nCopies(3, GuestTeacherEntity.builder().build());
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
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String emailAddress = RandomStringUtils.randomAlphabetic(10).concat("@littil.org");
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setSurname(surname);
        guestTeacher.setFirstName(firstName);
        guestTeacher.setEmail(emailAddress);
        guestTeacher.setLocale(locale);
        guestTeacher.setPostalCode(postalCode);

        final GuestTeacherEntity entity = GuestTeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .build();

        final GuestTeacher expectedGuestTeacher = new GuestTeacher();
        expectedGuestTeacher.setId(entity.getId());
        expectedGuestTeacher.setSurname(entity.getSurname());
        expectedGuestTeacher.setFirstName(entity.getFirstName());

        doReturn(entity).when(mapper).toEntity(guestTeacher);
        doReturn(false).when(repository).isPersistent(entity);

        assertThrows(PersistenceException.class, () -> service.saveTeacher(guestTeacher));
    }

    @Test
    void givenDeleteTeacher_thenShouldDeleteTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setId(teacherId);
        guestTeacher.setSurname(surname);
        guestTeacher.setFirstName(firstName);

        final GuestTeacherEntity entity = GuestTeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .build();

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);

        service.deleteTeacher(teacherId);

        then(repository).should().delete(entity);
    }

    @Test
    void givenDeleteUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);

        when(repository.findByIdOptional(teacherId)).thenReturn(Optional.empty());
        verifyNoMoreInteractions(repository);

        assertThrows(NotFoundException.class, () -> service.deleteTeacher(teacherId));
    }

    @Test
    void givenUpdateTeacher_thenShouldSuccessfullyUpdateTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String newSurname = RandomStringUtils.randomAlphabetic(10);
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String emailAddress = RandomStringUtils.randomAlphabetic(10).concat("@littil.org");
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setId(teacherId);
        guestTeacher.setSurname(newSurname);
        guestTeacher.setFirstName(firstName);

        final GuestTeacherEntity entity = GuestTeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .build();

        final GuestTeacher updatedGuestTeacher = new GuestTeacher();
        updatedGuestTeacher.setId(entity.getId());
        updatedGuestTeacher.setSurname(newSurname);
        updatedGuestTeacher.setFirstName(entity.getFirstName());
        updatedGuestTeacher.setEmail(emailAddress);

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);
        doReturn(updatedGuestTeacher).when(mapper).updateDomainFromEntity(entity, guestTeacher);

        GuestTeacher persisted = service.update(guestTeacher);

        then(mapper).should().updateEntityFromDomain(guestTeacher, entity);
        then(repository).should().persist(entity);
        assertThat(persisted.getId()).isEqualTo(teacherId);
        assertThat(persisted.getSurname()).isEqualTo(newSurname);
        assertThat(persisted).usingRecursiveComparison()
                .ignoringFields("surname")
                .isEqualTo(guestTeacher);
    }

    @Test
    void givenUpdateTeacherWithoutId_thenShouldThrowServiceException() {
        GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setEmail(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"));
        guestTeacher.setPostalCode(RandomStringUtils.randomAlphabetic(6));
        guestTeacher.setLocale(RandomStringUtils.randomAlphabetic(2));

        Validator validator = spy(Validator.class);
        doReturn(Collections.emptySet()).when(validator).validate(guestTeacher, GuestTeacher.class);

        then(repository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(ServiceException.class, () -> service.update(guestTeacher));
    }

    @Test
    void givenUpdateUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final GuestTeacher guestTeacher = new GuestTeacher();
        guestTeacher.setId(teacherId);
        guestTeacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        guestTeacher.setEmail(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"));
        guestTeacher.setLocale(RandomStringUtils.randomAlphabetic(2));
        guestTeacher.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.update(guestTeacher));
    }
}