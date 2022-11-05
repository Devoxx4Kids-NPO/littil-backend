package org.littil.api.guestTeacher.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;

import org.apache.commons.lang3.RandomStringUtils;

import org.junit.jupiter.api.Test;

import org.littil.api.location.repository.LocationEntity;
import org.littil.api.user.repository.UserEntity;

import java.time.DayOfWeek;
import java.util.*;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
class GuestTeacherRepositoryTest {

    @InjectSpy
    GuestTeacherRepository repository;

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
    void givenFindFilledExistingTeacherByName_thenShouldReturnSuccessfully() {
        String firstName = RandomStringUtils.randomAlphabetic(10);
        String surname = RandomStringUtils.randomAlphabetic(10);
        String prefix = RandomStringUtils.randomAlphabetic(10);
        LocationEntity location = new LocationEntity();
        LinkedHashSet<DayOfWeek> availibility = new LinkedHashSet<>(List.of(DayOfWeek.MONDAY));
        UserEntity user = new UserEntity();
        GuestTeacherEntity expectedTeacher = new GuestTeacherEntity(UUID.randomUUID(), firstName, surname, prefix, location, availibility, user);

        doReturn(List.of(expectedTeacher)).when(repository).list("surname like ?1", "%" + surname + "%");

        List<GuestTeacherEntity> foundTeacherList = repository.findBySurnameLike(surname);

        assertThat(foundTeacherList).isNotEmpty();

        GuestTeacherEntity foundTeacher = foundTeacherList.get(0);

        assertThat(expectedTeacher).usingRecursiveComparison().isEqualTo(foundTeacher);
    }

    @Test
    void givenFindByLocation_thenShouldReturnSuccessfully() {
        String firstName = RandomStringUtils.randomAlphabetic(10);
        String surname = RandomStringUtils.randomAlphabetic(10);
        Optional<GuestTeacherEntity> teacher = Optional.of(new GuestTeacherEntity(UUID.randomUUID(), firstName, surname, null, null, null, null));

        LocationEntity location = new LocationEntity();

        PanacheQuery<GuestTeacherEntity> query = Mockito.mock(PanacheQuery.class);
        when(query.page(Mockito.any())).thenReturn(query);
        when(query.firstResultOptional()).thenReturn(teacher);
        when(repository.find("location", location)).thenReturn(query);

        Optional<GuestTeacherEntity> foundTeacher = repository.findByLocation(location);

        assertThat(teacher).isEqualTo(foundTeacher);
    }

}