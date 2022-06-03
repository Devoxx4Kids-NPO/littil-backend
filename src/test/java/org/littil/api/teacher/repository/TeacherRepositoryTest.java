package org.littil.api.teacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class TeacherRepositoryTest {

    @InjectSpy
    TeacherRepository repository;

    @Test
    void givenFindExistingTeacherByName_thenShouldReturnSuccessfully() {
        String surname = RandomStringUtils.randomAlphabetic(10);
        List<TeacherEntity> teacher = List.of(new TeacherEntity(UUID.randomUUID(), surname, null, null, null, null, null, null));

        PanacheQuery<TeacherEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", surname);
        when(query.list()).thenReturn(teacher);

        List<TeacherEntity> foundTeacher = repository.findByName(surname);

        assertThat(teacher).isEqualTo(foundTeacher);
    }

    @Test
    void givenFindNonExistingTeacherByName_thenShouldReturnEmptyOptional() {
        final String searchSurname = RandomStringUtils.randomAlphabetic(10);

        PanacheQuery<TeacherEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", searchSurname);
        when(query.list()).thenReturn(Collections.emptyList());

        List<TeacherEntity> foundTeacher = repository.findByName(searchSurname);

        assertThat(foundTeacher).isEmpty();
    }
}