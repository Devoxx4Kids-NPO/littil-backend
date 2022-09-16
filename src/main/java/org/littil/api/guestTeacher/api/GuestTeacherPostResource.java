package org.littil.api.guestTeacher.api;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.EnumSet;

@Data
public class GuestTeacherPostResource {
    @NotEmpty(message = "{GuestTeacher.firstName.required}")
    private String firstName;

    @NotEmpty(message = "{GuestTeacher.surname.required}")
    private String surname;

    private String prefix;

    @NotEmpty(message = "{GuestTeacher.address.required}")
    private String address;

    @NotEmpty(message = "{GuestTeacher.postalCode.required}")
    private String postalCode;

    @NotEmpty(message = "{GuestTeacher.locale.required}")
    private String locale;

    private EnumSet<DayOfWeek> availability;
}
