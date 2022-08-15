package org.littil.api.auditing.repository;

import io.quarkus.security.identity.SecurityIdentity;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Optional;

@ApplicationScoped
public class AuditableEntityListener {

    @Inject
    UserRepository repository;

    @PrePersist
    public void prePersist(AbstractAuditableEntity entity) {
        entity.setCreatedBy(currentUserId());
    }

    @PreUpdate
    public void preUpdate(AbstractAuditableEntity entity) {
        entity.setLastModifiedBy(currentUserId());
    }

    private UserId currentUserId() {
        var identity = CDI.current().select(SecurityIdentity.class).get();
        if (identity != null) {
            Optional<UserEntity> user = repository.findByEmailAddress(identity.getAttribute("emailAddress"));
            return user.map(userEntity -> new UserId(userEntity.getId())).orElse(null);
        }
        return null;
    }
}
