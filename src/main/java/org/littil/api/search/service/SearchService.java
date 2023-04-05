package org.littil.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.module.service.Module;
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
                                               Optional<UserType> expectedUserType, List<String> expectedModules)  {
        List<SearchResult> searchResults = new ArrayList<>();
        if(expectedUserType.isEmpty()) {
                searchResults.addAll(getSearchResultForUserType(latitude, longitude, UserType.SCHOOL, expectedModules));
                searchResults.addAll(getSearchResultForUserType(latitude, longitude, UserType.GUEST_TEACHER, expectedModules));
        } else {
             searchResults = getSearchResultForUserType(latitude, longitude,
             expectedUserType.get(), expectedModules);
         }
        return searchResults;
    }
    private List<SearchResult> getSearchResultForUserType(final double latitude, final double longitude,
                                               UserType expectedUserType, List<String> expectedModules) {
        String condition = getCondition(expectedUserType);

        List<LocationSearchResult> searchResults = searchRepository.findLocationsOrderedByDistance(
                latitude, longitude, START_DISTANCE, MAX_DISTANCE, LIMIT, condition);
        return mapSearchResults(searchResults, expectedUserType, expectedModules);
    }

    private String getCondition(UserType expectedUserType) {
        return String.format(
                "(SELECT COUNT(*) FROM %s entity WHERE entity.location = location_id) > 0",
                expectedUserType.label
        );
    }

    private List<SearchResult> mapSearchResults(List<LocationSearchResult> locationSearchResults,
                                                UserType expectedUserType, List<String> expectedModules) {
        return switch(expectedUserType) {
            case SCHOOL -> mapLocationResultToSchoolSearchResult(locationSearchResults, expectedModules);
            case GUEST_TEACHER -> mapLocationResultToTeacherSearchResult(locationSearchResults, expectedModules);
        };
    }

    private List<SearchResult> mapLocationResultToSchoolSearchResult(List<LocationSearchResult> locationSearchResults, List<String> expectedModules) {
        List<SearchResult> searchResults = new ArrayList<>();

        for(LocationSearchResult location : locationSearchResults) {
            Optional<School> school = schoolService.getSchoolByLocation(location.getId());
            if(school.isPresent() && matchModules(school.get().getModules(), expectedModules)) {
                searchResults.add(searchMapper.toSchoolDomain(location, school.get(), UserType.SCHOOL));
            }
        }
        return searchResults;
    }

    private List<SearchResult> mapLocationResultToTeacherSearchResult(List<LocationSearchResult> locationSearchResults, List<String> expectedModules) {
        List<SearchResult> searchResults = new ArrayList<>();

        for(LocationSearchResult location : locationSearchResults) {
            Optional<GuestTeacher> teacher = teacherService.getTeacherByLocation(location.getId());
            if (teacher.isPresent() && matchModules(teacher.get().getModules(), expectedModules)) {
                searchResults.add(searchMapper.toGuestTeacherDomain(location, teacher.get(), UserType.GUEST_TEACHER));
            }
        }
        return searchResults;
    }

    private boolean matchModules(List<Module> activeModules, List<String> expectedModules) {
        if (expectedModules.isEmpty() || activeModules == null) {
            return true;
        }
        int nrOfMatchedModules = activeModules //
                .stream() //
                .map(Module::getName) //
                .filter(expectedModules::contains)
                .toList()
                .size();
        return nrOfMatchedModules != 0;
    }

}