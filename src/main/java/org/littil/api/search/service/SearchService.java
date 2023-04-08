package org.littil.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.littil.api.school.repository.SchoolRepository;
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

    private final GuestTeacherRepository teacherRepository;
    private final SchoolRepository schoolReposityory;
    private final SearchRepository searchRepository;
    private final SearchMapper searchMapper;
    private static final int START_DISTANCE = 1;
    private static final int LIMIT = 1000;


    public List<SearchResult> getSearchResults(final double latitude, final double longitude,
                                               Optional<UserType> expectedUserType, int maxDistance, List<String> expectedModules)  {
        List<SearchResult> searchResults = new ArrayList<>();
        if(expectedUserType.isEmpty()) {
                searchResults.addAll(getSearchResultForUserType(latitude, longitude, UserType.SCHOOL, maxDistance, expectedModules));
                searchResults.addAll(getSearchResultForUserType(latitude, longitude, UserType.GUEST_TEACHER, maxDistance,  expectedModules));
        } else {
             searchResults = getSearchResultForUserType(latitude, longitude,
             expectedUserType.get(), maxDistance, expectedModules);
         }
        return searchResults;
    }
    private List<SearchResult> getSearchResultForUserType(final double latitude, final double longitude,
                                                          UserType expectedUserType, int maxDistance, List<String> expectedModules) {
        String condition = getCondition(expectedUserType);

        List<LocationSearchResult> searchResults = searchRepository.findLocationsOrderedByDistance(
                latitude, longitude, START_DISTANCE, maxDistance, LIMIT, condition);
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
            Optional<SchoolEntity> school = schoolReposityory.findByLocationId(location.getId());
            if(school.isPresent() && matchModules(mapSchoolModulesToModules(school.get().getModules()), expectedModules)) {
                searchResults.add(searchMapper.toSchoolDomain(location, school.get(), UserType.SCHOOL));
            }
        }
        return searchResults;
    }

    private List<ModuleEntity> mapSchoolModulesToModules(List<SchoolModuleEntity> modules) {
        if (modules == null ) {
            return new ArrayList<>();
        }
        return modules.stream() //
                .map(SchoolModuleEntity::getModule)
                .filter(module -> !module.getDeleted())
                .toList();
    }

    private List<SearchResult> mapLocationResultToTeacherSearchResult(List<LocationSearchResult> locationSearchResults, List<String> expectedModules) {
        List<SearchResult> searchResults = new ArrayList<>();

        for(LocationSearchResult location : locationSearchResults) {
            Optional<GuestTeacherEntity> teacher = teacherRepository.findByLocationId(location.getId());
            if (teacher.isPresent() && matchModules(mapGuestTeacherModulesToModules(teacher.get().getModules()), expectedModules)) {
                searchResults.add(searchMapper.toGuestTeacherDomain(location, teacher.get(), UserType.GUEST_TEACHER));
            }
        }
        return searchResults;
    }

    private List<ModuleEntity> mapGuestTeacherModulesToModules(List<GuestTeacherModuleEntity> modules) {
        if (modules == null ) {
            return new ArrayList<>();
        }
        return modules.stream() //
                .map(GuestTeacherModuleEntity::getModule)
                .filter(module -> !module.getDeleted())
                .toList();
    }

    private boolean matchModules(List<ModuleEntity> activeModules, List<String> expectedModules) {
        if (expectedModules.isEmpty()) {
            return true;
        }

        int nrOfMatchedModules = activeModules //
                .stream() //
                .map(ModuleEntity::getName) //
                .filter(expectedModules::contains)
                .toList()
                .size();
        return nrOfMatchedModules != 0;
    }

}