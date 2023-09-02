package org.littil.api.contactPerson.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity(name = "ContactPerson")
@Table(name = "contact_person")
public class ContactPersonEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "contact_person_id", columnDefinition = "BINARY(16)")
    @NonNull
    private UUID id;

    @NotEmpty(message = "{ContactPerson.firstName.required}")
    @NonNull
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "prefix")
    private String prefix;

    @NotEmpty(message = "{ContactPerson.surname.required}")
    @NonNull
    @Column(name = "surname")
    private String surname;
}