package org.littil.api.auth.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {
    public Optional<UserEntity> findByEmailAddress(final String name) {
        return find("email_address", name).firstResultOptional();
    }
}
