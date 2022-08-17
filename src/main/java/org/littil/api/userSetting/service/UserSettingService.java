package org.littil.api.userSetting.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.littil.api.exception.ServiceException;
import org.littil.api.userSetting.repository.UserSettingEntity;
import org.littil.api.userSetting.repository.UserSettingRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor(onConstructor_ = {@Inject})
public class UserSettingService {

    UserSettingRepository repository;

    UserSettingMapper mapper;

    public Optional<UserSetting> getUserSettingByKey(final String key, final UUID userId) {
        return repository.findByIdOptional(new UserSettingEntity.UserSettingId(userId, key)).map(mapper::toDomain);
    }

    public List<UserSetting> findAll(@NonNull final UUID userId) {
        return repository.findAllByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Transactional
    public UserSetting update(@Valid UserSetting userSetting, final UUID userId) {
        if (Objects.isNull(userSetting.getKey())) {
            throw new ServiceException("UserSetting does not have a key");
        }

        UserSettingEntity entity = repository.findByIdOptional(new UserSettingEntity.UserSettingId(userId, userSetting.getKey()))
                .orElseThrow(() -> new NotFoundException(String.format("No UserSetting found for userId %s and key %s", userId.toString(), userSetting.getKey())));

        mapper.updateEntityFromDomain(userSetting, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, userSetting);
    }

    @Transactional
    public void delete(@NonNull final String key, final UUID userId) {
        Optional<UserSettingEntity> userSetting = repository.findByIdOptional(new UserSettingEntity.UserSettingId(userId, key));
        userSetting.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
    }

    @Transactional
    public UserSetting save(@Valid UserSetting userSetting, final UUID userId) {
        UserSettingEntity entity = mapper.toEntity(userSetting, userId);
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            return mapper.updateDomainFromEntity(entity, userSetting);
        } else {
            throw new PersistenceException();
        }
    }
}
