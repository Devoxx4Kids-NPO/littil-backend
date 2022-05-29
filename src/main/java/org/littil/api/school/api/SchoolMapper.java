package org.littil.api.school.api;

import org.littil.api.school.School;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface SchoolMapper {

    SchoolResource schoolToSchoolResource(School school);

    @Mapping(target = "id", ignore = true)
    School schoolResourceToSchool(SchoolUpsertResource school);
}
