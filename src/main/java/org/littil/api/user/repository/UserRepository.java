package org.littil.api.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {
    public Optional<UserEntity> findByEmailAddress(final String name) {
        return find("emailAddress", name).firstResultOptional();
    }

    public Optional<UserEntity> findByProviderId(final String providerId) {
        return find("providerId", providerId).firstResultOptional();
    }
}
