package org.littil.api.guestTeacher.service;

import org.littil.api.guestTeacher.api.GuestTeacherPostResource;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface GuestTeacherMapper {
    GuestTeacher toDomain(GuestTeacherPostResource guestTeacherPostResource);

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "postalCode", ignore = true)
    @Mapping(target = "locale", ignore = true)
    GuestTeacher toPurgedDomain(GuestTeacher guestTeacher);

    GuestTeacher toPurgedDomain(GuestTeacherEntity guestTeacherEntity);

    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.postalCode", target = "postalCode")
    GuestTeacher toDomain(GuestTeacherEntity guestTeacherEntity);

    @InheritInverseConfiguration(name = "toDomain")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    GuestTeacherEntity toEntity(GuestTeacher guestTeacher);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    void updateEntityFromDomain(GuestTeacher domain, @MappingTarget GuestTeacherEntity entity);

    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.postalCode", target = "postalCode")
    GuestTeacher updateDomainFromEntity(GuestTeacherEntity entity, @MappingTarget GuestTeacher domain);
}
