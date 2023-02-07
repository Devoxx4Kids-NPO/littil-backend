package org.littil.api.module.service;

import org.littil.api.module.repository.ModuleEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface ModuleMapper {

    Module toDomain(ModuleEntity moduleEntity);

    @InheritInverseConfiguration(name = "toDomain")
    @Mapping(target = "id", ignore = true)
    ModuleEntity toEntity(Module module);
}
