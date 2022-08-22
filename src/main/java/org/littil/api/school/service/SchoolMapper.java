package org.littil.api.school.service;

import org.littil.api.school.api.SchoolPostResource;
import org.littil.api.school.repository.SchoolEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface SchoolMapper {

    School toDomain(SchoolPostResource schoolResource);

    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.postalCode", target = "postalCode")
    School toDomain(SchoolEntity schoolEntity);

    @InheritInverseConfiguration(name = "toDomain")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    SchoolEntity toEntity(School school);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    void updateEntityFromDomain(School domain, @MappingTarget SchoolEntity entity);


    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.postalCode", target = "postalCode")
    School updateDomainFromEntity(SchoolEntity entity, @MappingTarget School domain);
}
