package org.littil.api.school.service;

import org.littil.api.school.repository.SchoolEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface SchoolMapper {

    School toDomain(SchoolEntity schoolEntity);

    @InheritInverseConfiguration(name = "toDomain")
    SchoolEntity toEntity(School school);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDomain(School domain, @MappingTarget SchoolEntity entity);

    School updateDomainFromEntity(SchoolEntity entity, @MappingTarget School domain);
}
