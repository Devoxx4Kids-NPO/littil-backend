package org.littil.api.school.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.contactPerson.repository.ContactPersonEntity;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.user.repository.UserEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "School")
@Table(name = "school")
public class SchoolEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "school_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{School.name.required}")
    @Column(name = "school_name")
    private String name;

    @NotNull(message = "{School.location.required}")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location", referencedColumnName = "location_id")
    private LocationEntity location;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_person", referencedColumnName = "contact_person_id")
    private ContactPersonEntity contactPerson;

    @OneToOne
    @JoinColumn(name = "user", referencedColumnName = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy="school")
    private List<SchoolModuleEntity> modules;

}
