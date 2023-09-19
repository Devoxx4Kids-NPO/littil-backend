package org.littil.api.guestTeacher.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.user.repository.UserEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "GuestTeacher")
@Table(name = "guest_teacher")
public class GuestTeacherEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "guest_teacher_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{GuestTeacher.firstName.required}")
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty(message = "{GuestTeacher.surname.required}")
    @Column(name = "surname")
    private String surname;

    @Column(name = "prefix")
    private String prefix;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location", referencedColumnName = "location_id")
    private LocationEntity location;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "guest_teacher_availability", joinColumns = @JoinColumn(name = "guest_teacher"))
    @Column(name = "availability")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availability = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", referencedColumnName = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy="guestTeacher")
    private List<GuestTeacherModuleEntity> modules;

}
