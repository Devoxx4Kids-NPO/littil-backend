package org.littil.api.school.repository;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class SchoolRepositoryTest {

    @InjectSpy
    SchoolRepository repository;

    @Test
    void givenFindExistingSchoolByName_thenShouldReturnSuccessfully() {
        String name = RandomStringUtils.randomAlphabetic(10);
        List<SchoolEntity> school = List.of(new SchoolEntity(UUID.randomUUID(), name, null, null, null));

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
}