package org.littil.api.contact.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.contactPerson.repository.ContactPersonEntity;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.littil.api.user.repository.UserEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Contact")
@Table(name = "contact")
public class ContactEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "contact_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "recipient", referencedColumnName = "user_id")
    private UserEntity recipient;
}
