package org.littil.api.userSetting.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.userSetting.repository.UserSettingEntity;
import org.littil.api.userSetting.repository.UserSettingRepository;

import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@QuarkusTest
class UserSettingServiceTest {

    @Inject
    UserSettingService service;

    @InjectMock
    UserSettingRepository repository;

    @InjectMock
    UserSettingMapper mapper;


    @Test
    void givenFindAllByUserId_thenShouldReturnUserSetting() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSettingEntity expectedUserSetting = new UserSettingEntity(userId, key, value);
        final UserSetting mappedUserSetting = new UserSetting(key, value);

        doReturn(List.of(expectedUserSetting)).when(repository).findAllByUserId(userId);
        doReturn(mappedUserSetting).when(mapper).toDomain(expectedUserSetting);

        final List<UserSetting> userSettings = service.findAll(userId);

        assertEquals(List.of(mappedUserSetting), userSettings);
    }

    @Test
    void givenWhenGettingUserSettingByKey_thenShouldReturnUserSetting() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSettingEntity expectedUserSettingEntity = new UserSettingEntity(userId, key, value);
        final UserSetting mappedUserSetting = new UserSetting(key, value);

        doReturn(Optional.of(expectedUserSettingEntity)).when(repository).findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        doReturn(mappedUserSetting).when(mapper).toDomain(expectedUserSettingEntity);

        final Optional<UserSetting> userSetting = service.getUserSettingByKey(key, userId);

        assertThat(userSetting).isPresent();
        assertEquals(mappedUserSetting, userSetting.get());
    }

    @Test
    void givenWhenGettingUserSettingByKeyWithNonMatchingUserId_thenShouldNotFound() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);

        doReturn(Optional.empty()).when(repository).findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        verifyNoMoreInteractions(mapper);

        final Optional<UserSetting> userSetting = service.getUserSettingByKey(key, userId);

        assertThat(userSetting).isNotPresent();
    }

    @Test
    void givenWhenUpdatingUserSetting_thenShouldUpdateAndReturnUpdatedDto() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);

        doReturn(Optional.empty()).when(repository).findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        verifyNoMoreInteractions(mapper);

        final Optional<UserSetting> userSetting = service.getUserSettingByKey(key, userId);

        assertThat(userSetting).isNotPresent();
    }

    @Test
    void givenDeleteUserSetting_thenShouldDeleteUserSetting() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSettingEntity userSettingEntity = new UserSettingEntity(userId, key, value);

        doReturn(Optional.of(userSettingEntity)).when(repository)
                .findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        service.delete(key, userId);
        then(repository).should().delete(userSettingEntity);
    }

    @Test
    void givenDeleteUnknownUserSetting_thenShouldThrowNotFoundException() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);

        doReturn(Optional.empty()).when(repository)
                .findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        verifyNoMoreInteractions(repository);

        assertThrows(NotFoundException.class, () -> service.delete(key, userId));
    }

    @Test
    void givenUpdateUserSetting_thenShouldSuccessfullyUpdateUserSetting() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);
        final String newValue = RandomStringUtils.randomAlphabetic(10);

        final UserSetting userSetting = new UserSetting(key, value);
        final UserSettingEntity userSettingEntity = new UserSettingEntity(userId, key, value);
        final UserSetting updatedUserSetting = new UserSetting(key, newValue);

        doReturn(Optional.of(userSettingEntity)).when(repository)
                .findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        doReturn(updatedUserSetting).when(mapper).updateDomainFromEntity(userSettingEntity, userSetting);

        UserSetting persisted = service.update(userSetting, userId);

        then(mapper).should().updateEntityFromDomain(userSetting, userSettingEntity);
        then(repository).should().persist(userSettingEntity);
        assertThat(persisted.getKey()).isEqualTo(key);
        assertThat(persisted.getValue()).isEqualTo(newValue);
        assertThat(persisted).usingRecursiveComparison()
                .ignoringFields("value")
                .isEqualTo(userSetting);
    }

    @Test
    void givenUpdateUnknownUserSetting_thenShouldThrowNotFoundException() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSetting userSetting = new UserSetting(key, value);

        doReturn(Optional.empty()).when(repository)
                .findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        then(repository).shouldHaveNoMoreInteractions();
        then(mapper).shouldHaveNoInteractions();

        assertThrows(NotFoundException.class, () -> service.update(userSetting, userId));
    }

    @Test
    void givenSaveUserSetting_thenShouldReturnPersistedUser() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSetting userSetting = new UserSetting(key, value);
        final UserSettingEntity userSettingEntity = new UserSettingEntity(userId, key, value);

        doReturn(true).when(repository).isPersistent(userSettingEntity);
        doReturn(userSettingEntity).when(mapper).toEntity(userSetting, userId);
        doReturn(userSetting).when(mapper).updateDomainFromEntity(userSettingEntity, userSetting);

        UserSetting persist = service.save(userSetting, userId);

        assertThat(persist).isEqualTo(userSetting);
    }

    @Test
    void givenSaveUserSettingUnknownErrorOccurred_thenShouldThrowPersistenceException() {
        final UUID userId = UUID.randomUUID();
        final String key = RandomStringUtils.randomAlphabetic(5);
        final String value = RandomStringUtils.randomAlphabetic(10);

        final UserSetting userSetting = new UserSetting(key, value);
        final UserSettingEntity userSettingEntity = new UserSettingEntity(userId, key, value);

        doReturn(userSettingEntity).when(mapper).toEntity(userSetting, userId);
        doReturn(false).when(repository).isPersistent(userSettingEntity);
        verifyNoMoreInteractions(mapper);

        assertThrows(PersistenceException.class, () -> service.save(userSetting, userId));
        verify(repository).persist(userSettingEntity);
    }

}