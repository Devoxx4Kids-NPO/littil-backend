package org.littil.api.auditing.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
@EntityListeners(AuditableEntityListener.class)
public abstract class AbstractAuditableEntity {
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @AttributeOverride(name = "id", column = @Column(name = "created_by"))
    @Embedded
    private UserId createdBy;

    @AttributeOverride(name = "id", column = @Column(name = "last_modified_by"))
    @Embedded
    private UserId lastModifiedBy;
}
