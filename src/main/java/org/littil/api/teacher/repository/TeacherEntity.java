package org.littil.api.teacher.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.location.repository.LocationEntity;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
public class TeacherEntity extends AbstractAuditableEntity {

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

    @NotEmpty(message = "{Teacher.location.required}")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location", referencedColumnName = "location_id")
    private LocationEntity location;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "teacher_availability", joinColumns = @JoinColumn(name = "teacher"))
    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availability = new HashSet<>();
}
