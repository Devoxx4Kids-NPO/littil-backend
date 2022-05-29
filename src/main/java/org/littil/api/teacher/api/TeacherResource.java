package org.littil.api.teacher.api;

import lombok.Value;

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
    int[] availability;
}
