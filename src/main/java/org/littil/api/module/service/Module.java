package org.littil.api.module.service;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class Module {
    private UUID id;

    @NotEmpty(message = "{Module.name.required}")
    @Column(name = "module_name")
    private String name;

}
