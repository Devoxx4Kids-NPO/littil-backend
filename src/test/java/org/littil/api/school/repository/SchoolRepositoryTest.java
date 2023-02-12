package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.littil.api.location.repository.LocationEntity;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
class SchoolRepositoryTest {

    @InjectSpy
    SchoolRepository repository;

    @Test
    void givenFindExistingSchoolByName_thenShouldReturnSuccessfully() {
        String name = RandomStringUtils.randomAlphabetic(10);
        List<SchoolEntity> school = List.of(new SchoolEntity(UUID.randomUUID(), name, null, null, null, null));

        Object[] params = { "%" + name + "%" } ;
        doReturn(school).when(repository).list(anyString(), eq(params));

        List<SchoolEntity> foundSchool = repository.findBySchoolNameLike(name);

        assertThat(school).isEqualTo(foundSchool);
    }

    @Test
    void givenFindNonExistingSchoolByName_thenShouldReturnEmptyOptional() {
        final String name = RandomStringUtils.randomAlphabetic(10);

        Object[] params = { "%" + name + "%" };
        doReturn(Collections.emptyList()).when(repository).list(anyString(), eq(params));

        List<SchoolEntity> foundSchool = repository.findBySchoolNameLike(name);

        assertThat(foundSchool).isEmpty();
    }

    @Test
    void givenFindByLocation_thenShouldReturnSuccessfully() {
        String name = RandomStringUtils.randomAlphabetic(10);
        Optional<SchoolEntity> school = Optional.of(new SchoolEntity(UUID.randomUUID(), name, null, null, null, null));

        LocationEntity location = new LocationEntity();

        PanacheQuery<SchoolEntity> query = Mockito.mock(PanacheQuery.class);
        when(query.page(Mockito.any())).thenReturn(query);
        when(query.firstResultOptional()).thenReturn(school);
        when(repository.find("location", location)).thenReturn(query);

        Optional<SchoolEntity> foundSchool = repository.findByLocation(location);

        assertThat(school).isEqualTo(foundSchool);
    }

}