package org.littil.api.guestTeacher.service;

import lombok.Data;
import org.littil.api.module.service.Module;

import javax.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Data
public class GuestTeacher {
    private UUID id;

    @NotEmpty(message = "{GuestTeacher.firstName.required}")
    private String firstName;

    @NotEmpty(message = "{GuestTeacher.surname.required}")
    private String surname;

    private String prefix;

    private String address;

    private String postalCode;

    private String locale;

    private EnumSet<DayOfWeek> availability;

    private List<Module> modules = new ArrayList<>();

}
