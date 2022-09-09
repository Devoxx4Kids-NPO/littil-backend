package org.littil.api.location.repository;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.littil.api.auditing.repository.AbstractAuditableEntity;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@ToString(callSuper = true)
@MappedSuperclass
@EntityListeners(LocationEntityListener.class)
public abstract class AbstractLocationEntity extends AbstractAuditableEntity {

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}