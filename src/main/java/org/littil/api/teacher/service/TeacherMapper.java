package org.littil.api.teacher.service;

import org.littil.api.teacher.repository.TeacherEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface TeacherMapper {

    Teacher toDomain(TeacherEntity teacherEntity);

    @InheritInverseConfiguration(name = "toDomain")
    TeacherEntity toEntity(Teacher teacher);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDomain(Teacher domain, @MappingTarget TeacherEntity entity);

    Teacher updateDomainFromEntity(TeacherEntity entity, @MappingTarget Teacher domain);
}
