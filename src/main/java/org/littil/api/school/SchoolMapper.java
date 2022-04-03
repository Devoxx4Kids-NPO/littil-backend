package org.littil.api.school;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface SchoolMapper {

    SchoolDto schoolToSchoolDto(School school);

    @Mapping(target = "id", ignore = true)
    School schoolDtoToSchool(SchoolDto schoolDto);
}
