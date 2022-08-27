package org.littil.api.userSetting.repository;

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
class UserSettingRepositoryTest {

    @InjectSpy
    UserSettingRepository repository;

    @Test
    void givenFindUserSettingsByUserId_thenShouldReturnSuccessfully() {
        final UUID userId = UUID.randomUUID();
        final List<UserSettingEntity> userSettings = List.of(new UserSettingEntity(userId, RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(10)));

        final PanacheQuery<UserSettingEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("user_id", userId);
        when(query.list()).thenReturn(userSettings);

        final List<UserSettingEntity> foundUserSettings = repository.findAllByUserId(userId);

        assertThat(userSettings).isEqualTo(foundUserSettings);
    }


    @Test
    void givenFindNonExistingTeacherByName_thenShouldReturnEmptyOptional() {
        final UUID userId = UUID.randomUUID();

        PanacheQuery<UserSettingEntity> query = mock(PanacheQuery.class);
        doReturn(query).when(repository).find("user_id", userId);
        when(query.list()).thenReturn(Collections.emptyList());

        List<UserSettingEntity> foundTeacher = repository.findAllByUserId(userId);

        assertThat(foundTeacher).isEmpty();
    }
}