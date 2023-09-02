package org.littil.api.guestTeacher.api;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.UUID;

@Data
public class GuestTeacherPostResource {

    private UUID id;

    @NotEmpty(message = "{GuestTeacher.firstName.required}")
    private String firstName;

    @NotEmpty(message = "{GuestTeacher.surname.required}")
    private String surname;

    private String prefix;

    @NotEmpty(message = "{GuestTeacher.address.required}")
    private String address;

    @NotEmpty(message = "{GuestTeacher.postalCode.required}")
    private String postalCode;

    private String locale;

    private EnumSet<DayOfWeek> availability;
}
