package org.littil.api.guestTeacher.service;

import org.littil.api.guestTeacher.repository.GuestTeacherModuleEntity;
import org.littil.api.module.service.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface GuestTeacherModuleMapper {

    List<Module> toDomain(List<GuestTeacherModuleEntity> modules);

    @Mapping(source = "module.id", target = "id")
    @Mapping(source = "module.name", target = "name")
    Module toDomain(GuestTeacherModuleEntity entity);
}
