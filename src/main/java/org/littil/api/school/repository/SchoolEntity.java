package org.littil.api.school.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "School")
@Table(name = "school")
public class SchoolEntity {

    @Id
    @GeneratedValue
    @Column(name = "school_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{School.name.required}")
    @Column(name = "school_name")
    private String name;

    @NotEmpty(message = "{School.address.required}")
    @Column(name = "address")
    private String address;

    @NotEmpty(message = "{School.postalCode.required}")
    @Column(name = "postal_code")
    private String postalCode;

    @NotEmpty(message = "{School.contactPersonName.required}")
    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Email(message = "{School.contactPersonEmail.invalid}")
    @Column(name = "contact_person_email")
    private String contactPersonEmail;
}
