package org.littil.api.teacher.api;

import lombok.Value;

@Value
public class TeacherUpsertResource {
    String firstName;
    String surname;
    String email;
    String postalCode;
    String country;
    String preferences;
    int[] availability;
}