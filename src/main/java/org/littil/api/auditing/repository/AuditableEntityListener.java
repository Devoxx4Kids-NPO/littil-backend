package org.littil.api.auditing.repository;

import io.quarkus.security.identity.SecurityIdentity;
import org.littil.api.auth.User;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AuditableEntityListener {
    @PrePersist
    public void prePersist(AbstractAuditableEntity entity) {
        entity.setCreatedBy(currentUserId());
    }

    @PreUpdate
    public void preUpdate(AbstractAuditableEntity entity) {
        entity.setLastModifiedBy(currentUserId());
    }

    private User currentUserId() {
        var identity = CDI.current().select(SecurityIdentity.class).get();
        if (identity != null) {
            return new User(identity.getAttribute("user_id"));
        }
        return null;
    }
}
