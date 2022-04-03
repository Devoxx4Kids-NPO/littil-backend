package org.littil.api.school;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class School {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String address;
    private String postalCode;
    private String contactPersonName;
    private String contactPersonEmail;
}
