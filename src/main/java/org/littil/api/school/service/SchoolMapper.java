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
    @Mapping(source = "contactPerson.firstName", target = "firstName")
    @Mapping(source = "contactPerson.prefix", target = "prefix")
    @Mapping(source = "contactPerson.surname", target = "surname")
    School toDomain(SchoolEntity schoolEntity);

    @InheritInverseConfiguration(name = "toDomain")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    @Mapping(target = "contactPerson.firstName", source = "firstName")
    @Mapping(target = "contactPerson.prefix", source = "prefix")
    @Mapping(target = "contactPerson.surname", source = "surname")
    SchoolEntity toEntity(School school);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location.address", source = "address")
    @Mapping(target = "location.postalCode", source = "postalCode")
    @Mapping(target = "contactPerson.firstName", source = "firstName")
    @Mapping(target = "contactPerson.prefix", source = "prefix")
    @Mapping(target = "contactPerson.surname", source = "surname")
    void updateEntityFromDomain(School domain, @MappingTarget SchoolEntity entity);

    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.postalCode", target = "postalCode")
    @Mapping(source = "contactPerson.firstName", target = "firstName")
    @Mapping(source = "contactPerson.prefix", target = "prefix")
    @Mapping(source = "contactPerson.surname", target = "surname")
    School updateDomainFromEntity(SchoolEntity entity, @MappingTarget School domain);

}
