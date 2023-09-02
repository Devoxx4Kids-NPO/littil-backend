package org.littil.api.module.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Module")
@Table(name = "module")
public class ModuleEntity extends AbstractAuditableEntity {
    @Id
    @Column(name = "module_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{Module.name.required}")
    @Column(name = "module_name")
    private String name;

    @Column(name = "deleted")
    private Boolean deleted;

}
