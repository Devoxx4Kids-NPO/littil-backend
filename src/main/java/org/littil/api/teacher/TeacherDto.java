package org.littil.api.teacher;

import lombok.Value;

@Value
public class TeacherDto {

    private final Long id;
    private final String firstName;
    private final String surname;
    private final String email;
    private final String postalCode;
    private final String country;
    private final String preferences;
    private final int[] availability;
}
