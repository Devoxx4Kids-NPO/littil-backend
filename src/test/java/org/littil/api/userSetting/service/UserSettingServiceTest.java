package org.littil.api.userSetting.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.userSetting.repository.UserSettingEntity;
import org.littil.api.userSetting.repository.UserSettingRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

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

        final UserSetting mappedUserSetting = new UserSetting();
        mappedUserSetting.setKey(key);
        mappedUserSetting.setValue(value);

        doReturn(List.of(expectedUserSetting)).when(repository).findAllByUserId(userId);
        doReturn(mappedUserSetting).when(mapper).toDomain(expectedUserSetting);

        final List<UserSetting> userSettings = service.findAll(userId);

        assertEquals(List.of(mappedUserSetting), userSettings);
    }
}