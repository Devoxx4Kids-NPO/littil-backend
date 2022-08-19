package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
class GuestGuestTeacherRepositoryTest {

    @InjectSpy
    GuestTeacherRepository repository;

    @Test
    void givenFindExistingTeacherByName_thenShouldReturnSuccessfully() {
        String surname = RandomStringUtils.randomAlphabetic(10);
        List<GuestTeacherEntity> teacher = List.of(new GuestTeacherEntity(UUID.randomUUID(), surname, null, null, null, null));

        PanacheQuery<GuestTeacherEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", surname);
        when(query.list()).thenReturn(teacher);

        List<GuestTeacherEntity> foundTeacher = repository.findBySurnameLike(surname);

        assertThat(teacher).isEqualTo(foundTeacher);
    }

    @Test
    void givenFindNonExistingTeacherByName_thenShouldReturnEmptyOptional() {
        final String searchSurname = RandomStringUtils.randomAlphabetic(10);

        PanacheQuery<GuestTeacherEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", searchSurname);
        when(query.list()).thenReturn(Collections.emptyList());

        List<GuestTeacherEntity> foundTeacher = repository.findBySurnameLike(searchSurname);

        assertThat(foundTeacher).isEmpty();
    }
}