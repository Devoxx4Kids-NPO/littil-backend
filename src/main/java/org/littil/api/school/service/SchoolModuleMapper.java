package org.littil.api.school.service;

import org.littil.api.module.service.Module;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface SchoolModuleMapper {

    // TODO refactor since Module doesn't have a deleted.
    List<Module> toDomain(List<SchoolModuleEntity> modules);

    @Mapping(source = "module.id", target = "id")
    @Mapping(source = "module.name", target = "name")
    Module toDomain(SchoolModuleEntity entity);
}
