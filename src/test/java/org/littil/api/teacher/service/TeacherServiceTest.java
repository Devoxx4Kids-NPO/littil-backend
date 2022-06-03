package org.littil.api.teacher.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ServiceException;
import org.littil.api.teacher.repository.TeacherEntity;
import org.littil.api.teacher.repository.TeacherRepository;

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
class TeacherServiceTest {

    @Inject
    TeacherService service;

    @InjectMock
    TeacherRepository repository;

    @InjectMock
    TeacherMapper mapper;

    @Test
    void givenGetTeacherByName_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final TeacherEntity expectedTeacher = TeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .build();
        final Teacher mappedTeacher = new Teacher();
        mappedTeacher.setId(teacherId);
        mappedTeacher.setSurname(surname);

        doReturn(List.of(expectedTeacher)).when(repository).findByName(surname);
        doReturn(mappedTeacher).when(mapper).toDomain(expectedTeacher);

        List<Teacher> teacher = service.getTeacherByName(surname);

        assertEquals(List.of(mappedTeacher), teacher);
    }

    @Test
    void givenGetTeacherByNullName_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getTeacherByName(null));
    }

    @Test
    void givenGetTeacherByUnknownName_thenShouldReturnEmptyList() {
        final String unknownName = RandomStringUtils.randomAlphabetic(10);

        doReturn(Collections.emptyList()).when(repository).findByName(unknownName);

        final List<Teacher> teacher = service.getTeacherByName(unknownName);

        assertTrue(teacher.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void givenGetTeacherById_thenShouldReturnTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final TeacherEntity expectedTeacher = TeacherEntity.builder()
                .id(teacherId)
                .build();
        final Teacher mappedTeacher = new Teacher();
        mappedTeacher.setId(teacherId);

        doReturn(Optional.of(expectedTeacher)).when(repository).findByIdOptional(teacherId);
        doReturn(mappedTeacher).when(mapper).toDomain(expectedTeacher);

        Optional<Teacher> teacher = service.getTeacherById(teacherId);

        assertEquals(Optional.of(mappedTeacher), teacher);
    }

    @Test
    void givenGetTeacherByNullId_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getTeacherById(null));
    }

    @Test
    void givenGetTeacherByUnknownId_thenShouldReturnEmptyOptional() {
        final UUID unknownId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(unknownId);

        final Optional<Teacher> teacher = service.getTeacherById(unknownId);

        assertInstanceOf(Optional.class, teacher);
        assertTrue(teacher.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void giveFindAll_thenShouldReturnTeacherList() {
        final List<TeacherEntity> expectedTeacherEntities = Collections.nCopies(3, TeacherEntity.builder().build());
        final List<Teacher> expectedTeachers = expectedTeacherEntities.stream().map(mapper::toDomain).toList();

        doReturn(expectedTeacherEntities).when(repository).listAll();

        List<Teacher> teachers = service.findAll();

        assertEquals(3, teachers.size());
        assertEquals(expectedTeachers, teachers);
    }

    @Test
    void giveFindAll_thenShouldReturnEmptyList() {
        final List<TeacherEntity> expectedTeacherEntities = Collections.emptyList();

        doReturn(expectedTeacherEntities).when(repository).listAll();

        List<Teacher> teachers = service.findAll();

        assertThat(teachers).isEmpty();
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

        final Teacher teacher = new Teacher();
        teacher.setSurname(surname);
        teacher.setFirstName(firstName);
        teacher.setEmail(emailAddress);
        teacher.setLocale(locale);
        teacher.setPostalCode(postalCode);

        final TeacherEntity entity = TeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .email(emailAddress)
                .locale(locale)
                .postalCode(postalCode)
                .build();

        final Teacher expectedTeacher = new Teacher();
        expectedTeacher.setId(entity.getId());
        expectedTeacher.setSurname(entity.getSurname());
        expectedTeacher.setFirstName(entity.getFirstName());
        expectedTeacher.setEmail(entity.getEmail());
        expectedTeacher.setLocale(entity.getLocale());
        expectedTeacher.setPostalCode(entity.getPostalCode());

        doReturn(entity).when(mapper).toEntity(teacher);
        doReturn(false).when(repository).isPersistent(entity);

        assertThrows(PersistenceException.class, () -> service.saveTeacher(teacher));
    }

    @Test
    void givenDeleteTeacher_thenShouldDeleteTeacher() {
        final UUID teacherId = UUID.randomUUID();
        final String surname = RandomStringUtils.randomAlphabetic(10);
        final String firstName = RandomStringUtils.randomAlphabetic(10);
        final String locale = RandomStringUtils.randomAlphabetic(2);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setSurname(surname);
        teacher.setFirstName(firstName);
        teacher.setLocale(locale);
        teacher.setPostalCode(postalCode);

        final TeacherEntity entity = TeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .locale(locale)
                .postalCode(postalCode)
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

        final Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setSurname(newSurname);
        teacher.setFirstName(firstName);
        teacher.setEmail(emailAddress);
        teacher.setLocale(locale);
        teacher.setPostalCode(postalCode);

        final TeacherEntity entity = TeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .firstName(firstName)
                .email(emailAddress)
                .locale(locale)
                .postalCode(postalCode)
                .build();

        final Teacher updatedTeacher = new Teacher();
        updatedTeacher.setId(entity.getId());
        updatedTeacher.setSurname(newSurname);
        updatedTeacher.setFirstName(entity.getFirstName());
        updatedTeacher.setEmail(emailAddress);
        updatedTeacher.setLocale(entity.getLocale());
        updatedTeacher.setPostalCode(entity.getPostalCode());

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(teacherId);
        doReturn(updatedTeacher).when(mapper).updateDomainFromEntity(entity, teacher);

        Teacher persisted = service.update(teacher);

        then(mapper).should().updateEntityFromDomain(teacher, entity);
        then(repository).should().persist(entity);
        assertThat(persisted.getId()).isEqualTo(teacherId);
        assertThat(persisted.getSurname()).isEqualTo(newSurname);
        assertThat(persisted).usingRecursiveComparison()
                .ignoringFields("surname")
                .isEqualTo(teacher);
    }

    @Test
    void givenUpdateTeacherWithoutId_thenShouldThrowServiceException() {
        Teacher teacher = new Teacher();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        teacher.setEmail(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"));
        teacher.setPostalCode(RandomStringUtils.randomAlphabetic(6));
        teacher.setLocale(RandomStringUtils.randomAlphabetic(2));

        Validator validator = spy(Validator.class);
        doReturn(Collections.emptySet()).when(validator).validate(teacher, Teacher.class);

        then(repository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(ServiceException.class, () -> service.update(teacher));
    }

    @Test
    void givenUpdateUnknownTeacher_thenShouldThrowNotFoundException() {
        final UUID teacherId = UUID.randomUUID();
        final Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setEmail(RandomStringUtils.randomAlphabetic(10).concat("@littil.org"));
        teacher.setLocale(RandomStringUtils.randomAlphabetic(2));
        teacher.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        doReturn(Optional.empty()).when(repository).findByIdOptional(teacherId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.update(teacher));
    }
}