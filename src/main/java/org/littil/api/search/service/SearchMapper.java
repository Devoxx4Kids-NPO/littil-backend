package org.littil.api.search.service;

import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.school.repository.SchoolEntity;
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
    SearchResult toSchoolDomain(LocationSearchResult searchResult, SchoolEntity school, UserType userType);

    @Mapping(source = "teacher.id", target = "id")
    @Mapping(target = "name", expression = "java(teacher.getFirstName() + \" \" + teacher.getPrefix() + \" \" + teacher.getSurname())")
    @Mapping(source = "searchResult.latitude", target = "latitude")
    @Mapping(source = "searchResult.longitude", target = "longitude")
    @Mapping(source = "searchResult.distance", target = "distance")
    @Mapping(source = "userType", target = "userType")
    SearchResult toGuestTeacherDomain(LocationSearchResult searchResult, GuestTeacherEntity teacher, UserType userType);

}