package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@Disabled("Disabled, needs a lot of refactoring")
class SchoolRepositoryTest {

    @InjectSpy
    SchoolRepository repository;

    @Test
    void givenFindExistingSchoolByName_thenShouldReturnSuccessfully() {
        String name = RandomStringUtils.randomAlphabetic(10);
        List<SchoolEntity> school = List.of(new SchoolEntity(UUID.randomUUID(), name, null, null, null));

        PanacheQuery<SchoolEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("school_name", name);
        when(query.list()).thenReturn(school);

        List<SchoolEntity> foundSchool = repository.findBySchoolNameLike(name);

        assertThat(school).isEqualTo(foundSchool);
    }

    @Test
    void givenFindNonExistingSchoolByName_thenShouldReturnEmptyOptional() {
        final String searchSurname = RandomStringUtils.randomAlphabetic(10);

        PanacheQuery<SchoolEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("surname", searchSurname);
        when(query.list()).thenReturn(Collections.emptyList());

        List<SchoolEntity> foundSchool = repository.findBySchoolNameLike(searchSurname);

        assertThat(foundSchool).isEmpty();
    }
}