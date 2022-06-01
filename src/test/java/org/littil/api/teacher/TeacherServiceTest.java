package org.littil.api.teacher;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.teacher.repository.TeacherEntity;
import org.littil.api.teacher.repository.TeacherRepository;
import org.littil.api.teacher.service.Teacher;
import org.littil.api.teacher.service.TeacherMapper;
import org.littil.api.teacher.service.TeacherService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

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
        final String surname = "Doe";
        final TeacherEntity expectedTeacher = TeacherEntity.builder()
                .id(teacherId)
                .surname(surname)
                .build();
        final Teacher mappedTeacher = new Teacher();
        mappedTeacher.setId(teacherId);
        mappedTeacher.setSurname(surname);

        doReturn(Optional.of(expectedTeacher)).when(repository).findByName(surname);
        doReturn(mappedTeacher).when(mapper).toDomain(expectedTeacher);

        Optional<Teacher> teacher = service.getTeacherByName(surname);

        assertEquals(Optional.of(mappedTeacher), teacher);
    }

    @Test
    void givenGetTeacherByUnknownName_thenShouldReturnEmptyOptional() {
        final String unknownName = "unknown_name";

        final Optional<Teacher> teacher = service.getTeacherByName(unknownName);

        assertInstanceOf(Optional.class, teacher);
        assertTrue(teacher.isEmpty());
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
    void givenGetTeacherByUnknownId_thenShouldReturnEmptyOptional() {
        final UUID unknownId = UUID.randomUUID();

        final Optional<Teacher> teacher = service.getTeacherById(unknownId);

        assertInstanceOf(Optional.class, teacher);
        assertTrue(teacher.isEmpty());
    }

    @Test
    @Disabled("WIP")
    void givenSaveTeacher_thenShouldReturnPersistedTeacher() {


    }

    @Test
    @Disabled("WIP")
    void givenSaveTeacherPersistenceFailed_thenShouldThrowNotFoundException() {

    }

    @Test
    @Disabled("WIP")
    void givenSaveTeacherUnknownErrorOccurred_thenShouldThrowPersistenceException() {

    }

    @Test
    void findAll() {
        final List<TeacherEntity> expectedTeachers = Collections.nCopies(3, TeacherEntity.builder().build());

//        doReturn(expectedTeachers).when(repository).listAll();
//        doReturn(expectedTeachers).when(mapper).();
//
//        List<Teacher> teachers = service.findAll();
//
//        assertEquals(3, teachers.size());
//        assertEquals(expectedTeachers, teachers);
    }

    @Test
    @Disabled("WIP")
    void deleteTeacher() {

    }
}