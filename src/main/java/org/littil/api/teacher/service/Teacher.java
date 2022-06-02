package org.littil.api.teacher.service;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.UUID;

@Data
public class Teacher {
    private UUID id;

    @NotNull(message = "{Teacher.firstName.required}")
    private String firstName;

    @NotNull(message = "{Teacher.surname.required}")
    private String surname;

    @Email(message = "{Teacher.email.invalid}")
    private String email;

    @NotNull(message = "{Teacher.postalCode.required}")
    private String postalCode;

    @NotNull(message = "{Teacher.locale.required}")
    private String locale;

    private String preferences;
    private EnumSet<DayOfWeek> availability;
}
