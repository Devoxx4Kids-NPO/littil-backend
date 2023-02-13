package org.littil.api.module.service;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class Module {
    private UUID id;

    @NotEmpty(message = "{Module.name.required}")
    private String name;

    private Boolean deleted;
}
