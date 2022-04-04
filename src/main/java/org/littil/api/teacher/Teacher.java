package org.littil.api.teacher;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Teacher {

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String surname;
    private String email;
    private String postalCode;
    private String country;
    private String preferences;
    //0 stands for Monday, 1 for Tuesday etc..
    //private int[] availability;
}
