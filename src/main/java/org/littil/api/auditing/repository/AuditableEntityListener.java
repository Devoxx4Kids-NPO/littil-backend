package org.littil.api.auditing.repository;

import org.littil.api.auth.TokenHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class AuditableEntityListener {

    @Inject
    TokenHelper tokenHelper;

    @PrePersist
    public void prePersist(AbstractAuditableEntity entity) {
        entity.setCreatedBy(currentUserId());
    }

    @PreUpdate
    public void preUpdate(AbstractAuditableEntity entity) {
        entity.setLastModifiedBy(currentUserId());
    }

    private UserId currentUserId() {
        return new UserId(tokenHelper.currentUserId().orElse(null));
    }
}
