package org.littil.api.teacher.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Teacher")
@Table(name = "teacher")
public class TeacherEntity {

    @Id
    @GeneratedValue
    @Column(name = "teacher_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{Teacher.firstName.required}")
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty(message = "{Teacher.surname.required}")
    @Column(name = "surname")
    private String surname;

    @Email(message = "{Teacher.email.invalid}")
    @Column(name = "email")
    private String email;

    @NotEmpty(message = "{Teacher.surname.required}")
    @Column(name = "postal_code")
    private String postalCode;

    @NotEmpty(message = "{Teacher.locale.required}")
    @Column(name = "locale")
    private String locale = "NL";

    @Column(name = "preferences")
    private String preferences;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "teacher_availability", joinColumns = @JoinColumn(name = "teacher"))
    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availability = new HashSet<>();
}
