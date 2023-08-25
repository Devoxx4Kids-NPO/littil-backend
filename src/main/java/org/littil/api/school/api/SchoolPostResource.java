package org.littil.api.school.api;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class SchoolPostResource {

    private UUID id;

    @NotEmpty(message = "{School.name.required}")
    private String name;

    @NotEmpty(message = "{School.address.required}")
    private String address;

    @NotEmpty(message = "{School.postalCode.required}")
    private String postalCode;

    @NotEmpty(message = "{School.firstName.required}")
    private String firstName;

    private String prefix;

    @NotEmpty(message = "{School.surname.required}")
    private String surname;
}
