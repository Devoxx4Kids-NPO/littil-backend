package org.littil.api.contactPerson;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class ContactPerson {
    
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{ContactPerson.firstName.required}")
    private String firstName;

    private String prefix;

    @NotEmpty(message = "{ContactPerson.surname.required}")
    private String surname;
}
