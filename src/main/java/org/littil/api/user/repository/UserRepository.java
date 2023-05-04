package org.littil.api.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {
    public Optional<UserEntity> findByEmailAddress(final String name) {
        return find("email_address", name).firstResultOptional();
    }

    public Optional<UserEntity> findByProviderId(final String providerId) {
        return find("provider_id", providerId).firstResultOptional();
    }
}
