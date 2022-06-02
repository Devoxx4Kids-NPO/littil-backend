package org.littil.api.school.service;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class School {
    private UUID id;

    @NotNull(message = "{School.name.required}")
    private String name;

    @NotNull(message = "{School.address.required}")
    private String address;

    @NotNull(message = "{School.postalCode.required}")
    private String postalCode;

    @NotNull(message = "{School.contactPersonName.required}")
    private String contactPersonName;

    @Email(message = "{School.contactPersonEmail.invalid}")
    private String contactPersonEmail;
}
