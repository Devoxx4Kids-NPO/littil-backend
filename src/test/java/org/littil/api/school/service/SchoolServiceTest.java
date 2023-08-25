package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.contactPerson.repository.ContactPersonEntity;
import org.littil.api.exception.ServiceException;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class SchoolServiceTest {

    @Inject
    SchoolService schoolService;

    @InjectMock
    SchoolRepository repository;

    @InjectMock
    SchoolMapper mapper;
    
    @InjectMock
    AuthenticationService authenticationService;
    
    @InjectMock
    UserService userService;
    
    @InjectMock
    LocationRepository locationRepository;

    @InjectMock
    TokenHelper tokenHelper;


    @Test
    void givenGetSchoolByName_thenShouldReturnSchool() {
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = RandomStringUtils.randomAlphabetic(10);
        final SchoolEntity expectedSchool = createSchoolEntity(schoolId, schoolName); 
        final School mappedSchool = createSchool(schoolId, schoolName);

        doReturn(List.of(expectedSchool)).when(repository).findBySchoolNameLike(schoolName);
        doReturn(mappedSchool).when(mapper).toDomain(expectedSchool);

        List<School> school = schoolService.getSchoolByName(schoolName);

        assertEquals(List.of(mappedSchool), school);
    }

    @Test
    void givenGetSchoolByNullName_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> schoolService.getSchoolByName(null));
    }

    @Test
    void givenGetSchoolByUnknownName_thenShouldReturnEmptyOptional() {
        final String unknownName = RandomStringUtils.randomAlphabetic(10);

        doReturn(Collections.emptyList()).when(repository).findBySchoolNameLike(unknownName);

        final List<School> school = schoolService.getSchoolByName(unknownName);

        assertInstanceOf(List.class, school);
        assertTrue(school.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void givenGetSchoolById_thenShouldReturnSchool() {
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = RandomStringUtils.randomAlphabetic(10);
        final SchoolEntity expectedSchool = createSchoolEntity(schoolId, schoolName);
        final School mappedSchool = createSchool(schoolId, schoolName);

        doReturn(Optional.of(expectedSchool)).when(repository).findByIdOptional(schoolId);
        doReturn(mappedSchool).when(mapper).toDomain(expectedSchool);

        Optional<School> school = schoolService.getSchoolById(schoolId);

        assertEquals(Optional.of(mappedSchool), school);
    }

    @Test
    void givenGetSchoolByNullId_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> schoolService.getSchoolById(null));
    }

    @Test
    void givenGetSchoolByUnknownId_thenShouldReturnEmptyOptional() {
        final UUID unknownId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(unknownId);

        final Optional<School> school = schoolService.getSchoolById(unknownId);

        assertInstanceOf(Optional.class, school);
        assertTrue(school.isEmpty());
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void giveFindAll_thenShouldReturnSchoolList() {
        final UUID schoolId = UUID.randomUUID();
        final String schoolName = RandomStringUtils.randomAlphabetic(10);
        final SchoolEntity expectedSchool = createSchoolEntity(schoolId, schoolName);
        final List<SchoolEntity> expectedSchoolEntities = Collections.nCopies(3, expectedSchool);
        final List<School> expectedSchools = expectedSchoolEntities.stream().map(mapper::toDomain).toList();

        doReturn(expectedSchoolEntities).when(repository).listAll();

        List<School> schools = schoolService.findAll();

        assertEquals(3, schools.size());
        assertEquals(expectedSchools, schools);
    }

    @Test
    void giveFindAll_thenShouldReturnEmptyList() {
        final List<SchoolEntity> expectedSchoolEntities = Collections.emptyList();

        doReturn(expectedSchoolEntities).when(repository).listAll();

        List<School> schools = schoolService.findAll();

        assertThat(schools).isEmpty();
        then(mapper).shouldHaveNoInteractions();
    }

    @Test
    void givenSaveSchool_thenShouldReturnSchool() {
        final UUID schoolId = UUID.randomUUID();
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonFirstName = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonSurname = RandomStringUtils.randomAlphabetic(10);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final ContactPersonEntity contactPerson = new ContactPersonEntity();
        contactPerson.setFirstName(contactPersonFirstName);
        contactPerson.setSurname(contactPersonSurname);

        final School school = new School();
        school.setName(name);
        school.setAddress(address);
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);
        school.setPostalCode(postalCode);

        final SchoolEntity entity = createSchoolEntity(schoolId, name);
        entity.setContactPerson(contactPerson);

        final School expectedSchool = new School();
        expectedSchool.setId(entity.getId());
        expectedSchool.setName(entity.getName());
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);

        final UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        doReturn(entity).when(mapper).toEntity(school);
        doReturn(Optional.of(user)).when(userService).getUserById(userId);
        doReturn(true).when(repository).isPersistent(entity);
        doNothing().when(locationRepository).persist(any(LocationEntity.class));

        doReturn(expectedSchool).when(mapper).updateDomainFromEntity(any(SchoolEntity.class), any(School.class));
        
        School savedSchool = schoolService.saveOrUpdate(school, userId);
        assertNotNull(savedSchool);
        assertEquals(expectedSchool,savedSchool);
        
        then(authenticationService).should().addAuthorization(userId, AuthorizationType.SCHOOL, schoolId);
    }
    
    @Test
    void givenSaveSchoolUnknownErrorOccurred_thenShouldThrowPersistenceException() {
        final UUID schoolId = UUID.randomUUID();
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonFirstName = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonSurname = RandomStringUtils.randomAlphabetic(10);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final ContactPersonEntity contactPerson = new ContactPersonEntity();
        contactPerson.setFirstName(contactPersonFirstName);
        contactPerson.setSurname(contactPersonSurname);

        final School school = new School();
        school.setName(name);
        school.setAddress(address);
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);
        school.setPostalCode(postalCode);

        final SchoolEntity entity = createSchoolEntity(schoolId, name);
        entity.setContactPerson(contactPerson);

        final UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        doReturn(entity).when(mapper).toEntity(school);
        doReturn(Optional.of(user)).when(userService).getUserById(userId);
        doReturn(false).when(repository).isPersistent(entity);
        doNothing().when(locationRepository).persist(any(LocationEntity.class));

        assertThrows(PersistenceException.class, () -> schoolService.saveOrUpdate(school, userId));
       
        then(authenticationService).shouldHaveNoInteractions();
    }

    @Test
    void givenSaveSchoolForUnknownUser_thenShouldThrowServiceException() {
        final UUID schoolId = UUID.randomUUID();
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonFirstName = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonSurname = RandomStringUtils.randomAlphabetic(10);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final ContactPersonEntity contactPerson = new ContactPersonEntity();
        contactPerson.setFirstName(contactPersonFirstName);
        contactPerson.setSurname(contactPersonSurname);

        final School school = new School();
        school.setName(name);
        school.setAddress(address);
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);
        school.setPostalCode(postalCode);

        final SchoolEntity entity = createSchoolEntity(schoolId, name);
        entity.setContactPerson(contactPerson);

        final UUID userId = UUID.randomUUID();
        
        doReturn(entity).when(mapper).toEntity(school);
        doReturn(Optional.empty()).when(userService).getUserById(userId);

        assertThrows(ServiceException.class, () -> schoolService.saveOrUpdate(school, UUID.randomUUID()));
    }
    
    @Test
    void givenDeleteSchool_thenShouldDeleteSchool() {
        final UUID schoolId = UUID.randomUUID();
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonFirstName = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonSurname = RandomStringUtils.randomAlphabetic(10);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final UUID userId = UUID.randomUUID();

        final School school = new School();
        school.setId(schoolId);
        school.setName(name);
        school.setAddress(address);
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);
        school.setPostalCode(postalCode);

        final SchoolEntity entity = createSchoolEntity(schoolId, name);

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(schoolId);

        schoolService.deleteSchool(schoolId, userId);

        then(repository).should().delete(entity);
        then(authenticationService).should().removeAuthorization(userId, AuthorizationType.SCHOOL, schoolId);
    }

    @Test
    void givenDeleteUnknownSchool_thenShouldThrowNotFoundException() {
        final UUID schoolId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findByIdOptional(schoolId);

        when(repository.findByIdOptional(schoolId)).thenReturn(Optional.empty());
        verifyNoMoreInteractions(repository);

        assertThrows(NotFoundException.class, () -> schoolService.deleteSchool(schoolId, userId));
        
        then(authenticationService).shouldHaveNoInteractions();  
        }

    @Test
    void givenDeleteNullSchool_thenShouldThrowNullPointer() {
        assertThrows(NullPointerException.class, () -> schoolService.deleteSchool(null, null));
    }

    @Test
    void givenUpdateSchool_thenShouldSuccessfullyUpdateSchool() {
        final UUID schoolId = UUID.randomUUID();
        final String newName = RandomStringUtils.randomAlphabetic(10);
        final String name = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonFirstName = RandomStringUtils.randomAlphabetic(10);
        final String contactPersonSurname = RandomStringUtils.randomAlphabetic(10);
        final String address = RandomStringUtils.randomAlphabetic(10);
        final String postalCode = RandomStringUtils.randomAlphabetic(6);

        final ContactPersonEntity contactPerson = new ContactPersonEntity();
        contactPerson.setFirstName(contactPersonFirstName);
        contactPerson.setSurname(contactPersonSurname);

        final School school = new School();
        school.setId(schoolId);
        school.setName(newName);
        school.setFirstName(contactPersonFirstName);
        school.setSurname(contactPersonSurname);
        school.setAddress(address);
        school.setPostalCode(postalCode);

        final SchoolEntity entity = createSchoolEntity(schoolId, name); 
        entity.setContactPerson(contactPerson);
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setAddress(address);
        locationEntity.setPostalCode(postalCode);
        entity.setLocation(locationEntity );

        final School updatedSchool = new School();
        updatedSchool.setId(entity.getId());
        updatedSchool.setName(newName);
        updatedSchool.setFirstName(entity.getContactPerson().getFirstName());
        updatedSchool.setSurname(entity.getContactPerson().getSurname());
        updatedSchool.setAddress(entity.getLocation().getAddress());
        updatedSchool.setPostalCode(entity.getLocation().getPostalCode());

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(schoolId);
        doReturn(updatedSchool).when(mapper).updateDomainFromEntity(entity, school);

        School persisted = schoolService.saveOrUpdate(school, null);

        then(mapper).should().updateEntityFromDomain(school, entity);
        then(repository).should().persist(entity);
        assertThat(persisted.getId()).isEqualTo(schoolId);
        assertThat(persisted.getName()).isEqualTo(newName);
        assertThat(persisted).usingRecursiveComparison()
                .ignoringFields("name")
                .isEqualTo(school);
    }
    
    @Test
    void givenUpdateSchoolWithoutId_thenShouldThrowServiceException() {
        School school = new School();
        school.setId(UUID.randomUUID());
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setFirstName(RandomStringUtils.randomAlphabetic(10));
        school.setSurname(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        then(repository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> schoolService.saveOrUpdate(school,null));
    }

    @Test
    void givenUpdateUnknownSchool_thenShouldThrowNotFoundException() {
        final UUID schoolId = UUID.randomUUID();
        final School school = new School();
        school.setId(schoolId);
        school.setName(RandomStringUtils.randomAlphabetic(10));
        school.setFirstName(RandomStringUtils.randomAlphabetic(10));
        school.setSurname(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(6));

        doReturn(Optional.empty()).when(repository).findByIdOptional(schoolId);
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> schoolService.saveOrUpdate(school, null));
    }

    @Test
    void givenUpdateNotOwnedSchool_thenShouldThrowUnauthorizedException() {
        final UUID schoolId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final String name = RandomStringUtils.randomAlphabetic(10);

        final School school = createSchool(schoolId, name);
        school.setFirstName(RandomStringUtils.randomAlphabetic(10));
        school.setSurname(RandomStringUtils.randomAlphabetic(10));
        school.setAddress(RandomStringUtils.randomAlphabetic(10));
        school.setPostalCode(RandomStringUtils.randomAlphabetic(10));

        final SchoolEntity entity = createSchoolEntity(schoolId, name);

        final UserEntity user = new UserEntity();
        user.setId(userId);
        entity.setUser(user);

        doReturn(Optional.of(entity)).when(repository).findByIdOptional(schoolId);
        doReturn(UUID.randomUUID()).when(tokenHelper).getCurrentUserId();

        assertThrows(UnauthorizedException.class, () -> schoolService.saveOrUpdate(school, userId));
    }

    private SchoolEntity createSchoolEntity(UUID schoolId, String schoolName) {
        SchoolEntity schoolEntity = new SchoolEntity();
        schoolEntity.setId(schoolId);
        schoolEntity.setName(schoolName);
        return schoolEntity;
    }

	private School createSchool(final UUID schoolId, final String schoolName) {
		final School mappedSchool = new School();
        mappedSchool.setId(schoolId);
        mappedSchool.setName(schoolName);
		return mappedSchool;
	}

}