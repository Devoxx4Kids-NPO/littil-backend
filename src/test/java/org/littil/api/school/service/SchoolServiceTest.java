package org.littil.api.school.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.exception.ServiceException;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@QuarkusTest
@Disabled("Disabled, needs a lot of refactoring")
class SchoolServiceTest {

    @Inject
    SchoolService service;

    @InjectMock
    SchoolRepository repository;

    @InjectMock
    SchoolMapper mapper;

//    @Test
//    void givenGetSchoolByName_thenShouldReturnSchool() {
//        final UUID schoolId = UUID.randomUUID();
//        final String surname = RandomStringUtils.randomAlphabetic(10);
//        final SchoolEntity expectedSchool = SchoolEntity.builder()
//                .id(schoolId)
//                .name(surname)
//                .build();
//        final School mappedSchool = new School();
//        mappedSchool.setId(schoolId);
//        mappedSchool.setName(surname);
//
//        doReturn(List.of(expectedSchool)).when(repository).findByName(surname);
//        doReturn(mappedSchool).when(mapper).toDomain(expectedSchool);
//
//        List<School> school = service.getSchoolByName(surname);
//
//        assertEquals(List.of(mappedSchool), school);
//    }

    @Test
    void givenGetSchoolByNullName_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getSchoolByName(null));
    }

    @Test
    void givenGetSchoolByUnknownName_thenShouldReturnEmptyOptional() {
        final String unknownName = RandomStringUtils.randomAlphabetic(10);

        doReturn(Collections.emptyList()).when(repository).findBySchoolNameLike(unknownName);

        final List<School> school = service.getSchoolByName(unknownName);

        assertInstanceOf(List.class, school);
        assertTrue(school.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

//    @Test
//    void givenGetSchoolById_thenShouldReturnSchool() {
//        final UUID schoolId = UUID.randomUUID();
//        final SchoolEntity expectedSchool = SchoolEntity.builder()
//                .id(schoolId)
//                .build();
//        final School mappedSchool = new School();
//        mappedSchool.setId(schoolId);
//
//        doReturn(Optional.of(expectedSchool)).when(repository).findByIdOptional(schoolId);
//        doReturn(mappedSchool).when(mapper).toDomain(expectedSchool);
//
//        Optional<School> school = service.getSchoolById(schoolId);
//
//        assertEquals(Optional.of(mappedSchool), school);
//    }

    @Test
    void givenGetSchoolByNullId_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> service.getSchoolById(null));
    }

    @Test
    void givenGetSchoolByUnknownId_thenShouldReturnEmptyOptional() {
        final UUID unknownId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(unknownId);

        final Optional<School> school = service.getSchoolById(unknownId);

        assertInstanceOf(Optional.class, school);
        assertTrue(school.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

//    @Test
//    void giveFindAll_thenShouldReturnSchoolList() {
//        final List<SchoolEntity> expectedSchoolEntities = Collections.nCopies(3, SchoolEntity.builder().build());
//        final List<School> expectedSchools = expectedSchoolEntities.stream().map(mapper::toDomain).toList();
//
//        doReturn(expectedSchoolEntities).when(repository).listAll();
//
//        List<School> schools = service.findAll();
//
//        assertEquals(3, schools.size());
//        assertEquals(expectedSchools, schools);
//    }

    @Test
    void giveFindAll_thenShouldReturnEmptyList() {
        final List<SchoolEntity> expectedSchoolEntities = Collections.emptyList();

        doReturn(expectedSchoolEntities).when(repository).listAll();

        List<School> schools = service.findAll();

        assertThat(schools).isEmpty();
        then(mapper).shouldHaveNoInteractions();
    }

//    @Test
//    void givenSaveSchoolUnknownErrorOccurred_thenShouldThrowPersistenceException() {
//        final UUID schoolId = UUID.randomUUID();
//        final String name = RandomStringUtils.randomAlphabetic(10);
//        final String address = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonName = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonEmail = RandomStringUtils.randomAlphabetic(10).concat("@littil.org");
//        final String postalCode = RandomStringUtils.randomAlphabetic(6);
//
//        final School school = new School();
//        school.setName(name);
//        school.setAddress(address);
//        school.setContactPersonName(contactPersonName);
//        school.setContactPersonEmail(contactPersonEmail);
//        school.setPostalCode(postalCode);
//
//        final SchoolEntity entity = SchoolEntity.builder()
//                .id(schoolId)
//                .name(name)
//                .contactPersonName(contactPersonName)
//                .build();
//
//        final School expectedSchool = new School();
//        expectedSchool.setId(entity.getId());
//        expectedSchool.setName(entity.getName());
//        expectedSchool.setContactPersonName(entity.getContactPersonName());
//
//        doReturn(entity).when(mapper).toEntity(school);
//        doReturn(false).when(repository).isPersistent(entity);
//
//        assertThrows(PersistenceException.class, () -> service.saveSchool(school, RandomStringUtils.randomAlphabetic(10)));
//    }
//
//    @Test
//    void givenDeleteSchool_thenShouldDeleteSchool() {
//        final UUID schoolId = UUID.randomUUID();
//        final String name = RandomStringUtils.randomAlphabetic(10);
//        final String address = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonName = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonEmail = RandomStringUtils.randomAlphabetic(10).concat("@littil.org");
//        final String postalCode = RandomStringUtils.randomAlphabetic(6);
//
//        final School school = new School();
//        school.setId(schoolId);
//        school.setName(name);
//        school.setAddress(address);
//        school.setContactPersonName(contactPersonName);
//        school.setContactPersonEmail(contactPersonEmail);
//        school.setPostalCode(postalCode);
//
//        final SchoolEntity entity = SchoolEntity.builder()
//                .id(schoolId)
//                .name(name)
//                .contactPersonName(contactPersonName)
//                .build();
//
//        doReturn(Optional.of(entity)).when(repository).findByIdOptional(schoolId);
//
//        service.deleteSchool(schoolId);
//
//        then(repository).should().delete(entity);
//    }
//
//    @Test
//    void givenDeleteUnknownSchool_thenShouldThrowNotFoundException() {
//        final UUID schoolId = UUID.randomUUID();
//
//        doReturn(Optional.empty()).when(repository).findByIdOptional(schoolId);
//
//        when(repository.findByIdOptional(schoolId)).thenReturn(Optional.empty());
//        verifyNoMoreInteractions(repository);
//
//        assertThrows(NotFoundException.class, () -> service.deleteSchool(schoolId));
//    }
//
//    @Test
//    void givenUpdateSchool_thenShouldSuccessfullyUpdateSchool() {
//        final UUID schoolId = UUID.randomUUID();
//        final String newName = RandomStringUtils.randomAlphabetic(10);
//        final String name = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonName = RandomStringUtils.randomAlphabetic(10);
//        final String contactPersonEmail = RandomStringUtils.randomAlphabetic(10).concat("@littil.org");
//        final String postalCode = RandomStringUtils.randomAlphabetic(6);
//        final String address = RandomStringUtils.randomAlphabetic(10);
//
//        final School school = new School();
//        school.setId(schoolId);
//        school.setName(newName);
//        school.setContactPersonName(contactPersonName);
//        school.setContactPersonEmail(contactPersonEmail);
//        school.setAddress(address);
//        school.setPostalCode(postalCode);
//
//        final SchoolEntity entity = SchoolEntity.builder()
//                .id(schoolId)
//                .name(name)
//                .contactPersonName(contactPersonName)
//                .build();
//
//        final School updatedSchool = new School();
//        updatedSchool.setId(entity.getId());
//        updatedSchool.setName(newName);
//        updatedSchool.setContactPersonName(entity.getContactPersonName());
//
//        doReturn(Optional.of(entity)).when(repository).findByIdOptional(schoolId);
//        doReturn(updatedSchool).when(mapper).updateDomainFromEntity(entity, school);
//
//        School persisted = service.update(school);
//
//        then(mapper).should().updateEntityFromDomain(school, entity);
//        then(repository).should().persist(entity);
//        assertThat(persisted.getId()).isEqualTo(schoolId);
//        assertThat(persisted.getName()).isEqualTo(newName);
//        assertThat(persisted).usingRecursiveComparison()
//                .ignoringFields("name")
//                .isEqualTo(school);
//    }

    @Test
    void givenUpdateSchoolWithoutId_thenShouldThrowServiceException() {
        School school = new School();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setContactPersonName(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        Validator validator = spy(Validator.class);
        doReturn(Collections.emptySet()).when(validator).validate(school, School.class);

        then(repository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(ServiceException.class, () -> service.update(school));
    }

    @Test
    void givenUpdateUnknownSchool_thenShouldThrowNotFoundException() {
        final UUID schoolId = UUID.randomUUID();
        final School school = new School();
        school.setId(schoolId);
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setContactPersonName(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        doReturn(Optional.empty()).when(repository).findByIdOptional(schoolId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.update(school));
    }
}