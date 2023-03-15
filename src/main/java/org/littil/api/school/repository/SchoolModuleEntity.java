package org.littil.api.school.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.module.repository.ModuleEntity;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "SchoolModule")
@Table(name = "school_module")
public class SchoolModuleEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "module_id", referencedColumnName = "module_id")
    private ModuleEntity module;

    @ManyToOne(optional = false)
    @JoinColumn(name="school_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SchoolEntity school;

}
