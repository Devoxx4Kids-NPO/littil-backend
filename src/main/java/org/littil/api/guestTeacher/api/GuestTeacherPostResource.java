package org.littil.api.guestTeacher.api;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.EnumSet;

@Data
public class GuestTeacherPostResource {
    @NotEmpty(message = "{Teacher.firstName.required}")
    private String firstName;

    @NotEmpty(message = "{Teacher.surname.required}")
    private String surname;

    @NotEmpty(message = "{Teacher.email.required}")
    @Email(message = "{Teacher.email.invalid}")
    private String email;

    @NotEmpty(message = "{Teacher.postalCode.required}")
    private String postalCode;

    @NotEmpty(message = "{Teacher.locale.required}")
    private String locale;

    private String preferences;
    private EnumSet<DayOfWeek> availability;
}
