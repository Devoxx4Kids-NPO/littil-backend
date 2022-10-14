package org.littil.api.search.service;

import org.littil.api.school.service.School;
import org.littil.api.search.api.UserType;
import org.littil.api.search.repository.LocationSearchResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface SearchMapper {

    @Mapping(source = "school.id", target = "id")
    @Mapping(source = "school.name", target = "name")
    @Mapping(source = "searchResult.latitude", target = "latitude")
    @Mapping(source = "searchResult.longitude", target = "longitude")
    @Mapping(source = "searchResult.distance", target = "distance")
    @Mapping(source = "userType", target = "userType")
    SearchResult toSchoolDomain(LocationSearchResult searchResult, School school, UserType userType);
}