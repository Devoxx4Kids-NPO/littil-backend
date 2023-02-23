package org.littil.api.school.service;

import lombok.Data;

import org.littil.api.module.service.Module;

import javax.validation.constraints.NotEmpty;
import java.util.*;

@Data
public class School {
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

    private List<Module> modules = new ArrayList<>();
}
