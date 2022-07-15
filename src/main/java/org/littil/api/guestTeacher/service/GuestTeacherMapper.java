package org.littil.api.guestTeacher.service;

import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface GuestTeacherMapper {

    GuestTeacher toDomain(GuestTeacherEntity guestTeacherEntity);

    @InheritInverseConfiguration(name = "toDomain")
    GuestTeacherEntity toEntity(GuestTeacher guestTeacher);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDomain(GuestTeacher domain, @MappingTarget GuestTeacherEntity entity);

    GuestTeacher updateDomainFromEntity(GuestTeacherEntity entity, @MappingTarget GuestTeacher domain);
}
