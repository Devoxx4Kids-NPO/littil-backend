package org.littil.api.teacher.api;

import lombok.Value;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.UUID;

@Value
public class TeacherResource {

    UUID id;
    String firstName;
    String surname;
    String email;
    String postalCode;
    String country;
    String preferences;
    EnumSet<DayOfWeek> availability;
}
