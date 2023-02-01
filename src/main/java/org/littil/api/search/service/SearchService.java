package org.littil.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolService;
import org.littil.api.search.api.UserType;
import org.littil.api.search.repository.LocationSearchResult;
import org.littil.api.search.repository.SearchRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final GuestTeacherService teacherService;
    private final SchoolService schoolService;
    private final SearchRepository searchRepository;
    private final SearchMapper searchMapper;
    private static final int START_DISTANCE = 1;
    private static final int MAX_DISTANCE = 10000;
    private static final int LIMIT = 100;

    public List<SearchResult> getSearchResults(final double latitude, final double longitude,
                                               UserType expectedUserType) {
        String condition = getCondition(expectedUserType);

        List<LocationSearchResult> searchResults = searchRepository.findLocationsOrderedByDistance(
                latitude, longitude, START_DISTANCE, MAX_DISTANCE, LIMIT, condition);
        return mapSearchResults(searchResults, expectedUserType);
    }

    private String getCondition(UserType expectedUserType) {
        return String.format(
                "(SELECT COUNT(*) FROM %s entity WHERE entity.location = location_id) > 0",
                expectedUserType.label
        );
    }

    private List<SearchResult> mapSearchResults(List<LocationSearchResult> locationSearchResults,
                                                UserType expectedUserType) {
        return switch(expectedUserType) {
            case SCHOOL -> mapLocationResultToSchoolSearchResult(locationSearchResults);
            case GUEST_TEACHER -> mapLocationResultToTeacherSearchResult(locationSearchResults);
        };
    }

    private List<SearchResult> mapLocationResultToSchoolSearchResult(List<LocationSearchResult> locationSearchResults) {
        List<SearchResult> searchResults = new ArrayList<>();

        for(LocationSearchResult location : locationSearchResults) {
            Optional<School> school = schoolService.getSchoolByLocation(location.getId());
            if(school.isEmpty()) break;
            searchResults.add(searchMapper.toSchoolDomain(location, school.get(), UserType.SCHOOL));
        }
        return searchResults;
    }

    private List<SearchResult> mapLocationResultToTeacherSearchResult(List<LocationSearchResult> locationSearchResults) {
        List<SearchResult> searchResults = new ArrayList<>();

        for(LocationSearchResult location : locationSearchResults) {
            Optional<GuestTeacher> teacher = teacherService.getTeacherByLocation(location.getId());
            if(teacher.isEmpty()) break;
            searchResults.add(searchMapper.toGuestTeacherDomain(location, teacher.get(), UserType.GUEST_TEACHER));
        }
        return searchResults;
    }

}