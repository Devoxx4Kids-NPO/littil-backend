package org.littil.api.teacher;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.*;

@Data
@NoArgsConstructor
@Entity
public class Teacher {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String firstName;
    private String surname;
    private String email;
    private String postalCode;
    private String country;
    private String preferences;

    @ElementCollection
    private Set<DayOfWeek> availability = new HashSet<>();
}
