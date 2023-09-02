package org.littil.api.auditing.repository;

import org.littil.api.auth.TokenHelper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@ApplicationScoped
public class AuditableEntityListener {

    @Inject
    TokenHelper tokenHelper;

    @PrePersist
    public void prePersist(AbstractAuditableEntity entity) {
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(currentUserId());
        }
    }

    @PreUpdate
    public void preUpdate(AbstractAuditableEntity entity) {
        entity.setLastModifiedBy(currentUserId());
    }

    private UserId currentUserId() {
        return new UserId(tokenHelper.currentUserId().orElse(null));
    }
}
