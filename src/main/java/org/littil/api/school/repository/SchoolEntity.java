package org.littil.api.school.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.location.repository.LocationEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "School")
@Table(name = "school")
public class SchoolEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "school_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{School.name.required}")
    @Column(name = "school_name")
    private String name;

    @NotEmpty(message = "{School.contactPersonName.required}")
    @Column(name = "contact_person_name")
    private String contactPersonName;

    @NotEmpty(message = "{School.location.required}")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location", referencedColumnName = "location_id")
    private LocationEntity location;
}
