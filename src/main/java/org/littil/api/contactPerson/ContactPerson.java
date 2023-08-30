package org.littil.api.contactPerson;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class ContactPerson {
    
    private UUID id;

    @NotEmpty(message = "{ContactPerson.firstName.required}")
    private String firstName;

    private String prefix;

    @NotEmpty(message = "{ContactPerson.surname.required}")
    private String surname;
}
