package org.littil.api.userSetting.service;

import org.littil.api.userSetting.repository.UserSettingEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "cdi")
abstract class UserSettingMapper {
    abstract UserSetting toDomain(UserSettingEntity userSettingEntity);

    @InheritInverseConfiguration(name = "toDomain")
    UserSettingEntity toEntity(final UserSetting userSetting, final UUID userId) {
        return new UserSettingEntity(userId, userSetting.getKey(), userSetting.getValue());
    }

    abstract void updateEntityFromDomain(UserSetting domain, @MappingTarget UserSettingEntity entity);

    abstract UserSetting updateDomainFromEntity(UserSettingEntity entity, @MappingTarget UserSetting domain);
}
