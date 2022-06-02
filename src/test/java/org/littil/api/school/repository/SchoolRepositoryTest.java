package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
class SchoolRepositoryTest {

    @InjectSpy
    SchoolRepository repository;

    @Test
    void givenFindExistingSchoolByName_thenShouldReturnSuccessfully() {
        String name = RandomStringUtils.randomAlphabetic(10);
        Optional<SchoolEntity> school = Optional.of(new SchoolEntity(UUID.randomUUID(), name, null, null, null, null));

        PanacheQuery<SchoolEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("school_name", name);
        when(query.firstResultOptional()).thenReturn(school);

        Optional<SchoolEntity> foundSchool = repository.findByName(name);

        assertThat(school).isEqualTo(foundSchool);
    }

    @Test
    void givenFindNonExistingSchoolByName_thenShouldReturnEmptyOptional() {
        final String searchSurname = RandomStringUtils.randomAlphabetic(10);

        PanacheQuery<SchoolEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", searchSurname);
        when(query.firstResultOptional()).thenReturn(Optional.empty());

        Optional<SchoolEntity> foundSchool = repository.findByName(searchSurname);

        assertThat(foundSchool).isEmpty();
    }
}