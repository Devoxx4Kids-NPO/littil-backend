package org.littil.api.teacher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    //0 stands for Monday, 1 for Tuesday etc..
    private int[] availability;
}
