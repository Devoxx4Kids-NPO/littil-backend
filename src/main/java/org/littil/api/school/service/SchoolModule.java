package org.littil.api.school.service;

import lombok.Data;
import org.littil.api.module.service.Module;

import java.util.UUID;

@Data
public class SchoolModule {

    private UUID id;

    private Module module;

    private Boolean deleted;

}