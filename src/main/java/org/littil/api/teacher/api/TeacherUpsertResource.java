package org.littil.api.teacher.api;

import lombok.Value;

import java.time.DayOfWeek;
import java.util.EnumSet;

@Value
public class TeacherUpsertResource {
    String firstName;
    String surname;
    String email;
    String postalCode;
    String country;
    String preferences;
    EnumSet<DayOfWeek> availability;
}