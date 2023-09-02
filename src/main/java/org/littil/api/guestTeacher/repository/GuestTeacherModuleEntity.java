package org.littil.api.guestTeacher.repository;

import lombok.*;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.module.repository.ModuleEntity;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "GuestTeacherModule")
@Table(name = "guest_teacher_module")
public class GuestTeacherModuleEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    private ModuleEntity module;

    @ManyToOne(optional = false)
    @JoinColumn(name="guest_teacher_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GuestTeacherEntity guestTeacher;

}
