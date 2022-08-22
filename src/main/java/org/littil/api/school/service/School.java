package org.littil.api.school.service;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class School {
    private UUID id;

    @NotEmpty(message = "{School.name.required}")
    private String name;

    @NotEmpty(message = "{School.address.required}")
    private String address;

    @NotEmpty(message = "{School.postalCode.required}")
    private String postalCode;

    @NotEmpty(message = "{School.contactPersonName.required}")
    private String contactPersonName;
}
