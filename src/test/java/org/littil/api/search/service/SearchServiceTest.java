package org.littil.api.search.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class SearchServiceTest {

    public static final int MAX_DISTANCE = 100;
    @Inject
    SearchService searchService;
    
    @InjectMock
    GuestTeacherRepository teacherRepository;
    
    @InjectMock
    SchoolRepository schoolRepository;

    @InjectMock
    SearchRepository searchRepository;
    

    @ParameterizedTest
    @MethodSource("provideArgumentsForWhenGetSearchResultsTest")
    public void whenGetSearchResults_ReturnExpectedSearchResult (List<SchoolEntity> schoolList, List<GuestTeacherEntity> teacherList,
                  Optional<UserType> expectedUserType, List<String> expectedModules, int expectedResults) {

        List<LocationSearchResult> locationSearchResult = new ArrayList<>();
        for (SchoolEntity school : schoolList) {
            UUID locationId = UUID.randomUUID();
            LocationSearchResult location = new LocationSearchResult();
            location.setId(locationId);
            locationSearchResult.add(location);
            doReturn(Optional.of(school)).when(schoolRepository).findByLocationId(locationId);
        }

        for (GuestTeacherEntity teacher : teacherList) {
            UUID locationId = UUID.randomUUID();
            LocationSearchResult location = new LocationSearchResult();
            location.setId(locationId);
            locationSearchResult.add(location);
            doReturn(Optional.of(teacher)).when(teacherRepository).findByLocationId(locationId);
        }

        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
                eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());
        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, expectedUserType,MAX_DISTANCE, expectedModules);
        assertNotNull(searchResults);
        assertEquals(expectedResults, searchResults.size());

        if (locationSearchResult.size() == expectedResults) {
            List<String> searchResultNames = searchResults.stream().map(SearchResult::getName).toList();
            for (SchoolEntity school : schoolList) {
                assertTrue(searchResultNames.contains(school.getName()));
            }
            for (GuestTeacherEntity teacher : teacherList) {
                assertTrue(searchResultNames.contains(teacher.getFirstName() + " " + teacher.getPrefix()+ " " + teacher.getSurname()));
            }
        }
    }

    public static Stream<Arguments> provideArgumentsForWhenGetSearchResultsTest() {
        SchoolEntity school1 = getSchool(List.of("Scratch"));
        SchoolEntity school2 = getSchool(new ArrayList<>());
        List<SchoolEntity> allSchools = List.of(school1, school2);
        GuestTeacherEntity teacher1 = getGuestTeacher(List.of("Scratch"));
        GuestTeacherEntity teacher2 = getGuestTeacher(new ArrayList<>());

        List emptyList = new ArrayList<>();

        return Stream.of(
                Arguments.of(List.of(school1), emptyList, Optional.of(UserType.SCHOOL), emptyList, 1),
                Arguments.of(allSchools, emptyList, Optional.of(UserType.SCHOOL), emptyList, 2),
                Arguments.of(emptyList, List.of(teacher1), Optional.of(UserType.GUEST_TEACHER), emptyList, 1),
                Arguments.of(emptyList, List.of(teacher1, teacher2), Optional.of(UserType.GUEST_TEACHER), emptyList, 2),
                Arguments.of(List.of(school1, school2), List.of(teacher1, teacher2), Optional.empty(), emptyList, 4),
                Arguments.of(List.of(school1), List.of(teacher1, teacher2), Optional.of(UserType.GUEST_TEACHER), emptyList, 2),
                Arguments.of(List.of(school1, school2), emptyList, Optional.of(UserType.SCHOOL), List.of("Scratch"), 1),
                Arguments.of(emptyList, List.of(teacher1, teacher2), Optional.of(UserType.GUEST_TEACHER), List.of("Scratch"), 1)
        );
    }

    private static SchoolEntity getSchool(List<String> moduleNames) {
        SchoolEntity school = getSchool();
        List<SchoolModuleEntity> schoolModules = new ArrayList<>();
        for (String moduleName : moduleNames) {
            SchoolModuleEntity schoolModuleEntity = new SchoolModuleEntity();
            schoolModuleEntity.setId(UUID.randomUUID());
            schoolModuleEntity.setModule(createModuleEntity(moduleName));
            schoolModules.add(schoolModuleEntity);
        }
        school.setModules(schoolModules);
        return school;
    }

    private static SchoolEntity getSchool() {
        SchoolEntity school = new SchoolEntity();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        return school;
    }

    private static GuestTeacherEntity getGuestTeacher(List<String> moduleNames) {
        GuestTeacherEntity teacher = getGuestTeacher();
        List<GuestTeacherModuleEntity> teacherModules = new ArrayList<>();
        for (String moduleName : moduleNames) {
            GuestTeacherModuleEntity guestTeacherModuleEntity = new GuestTeacherModuleEntity();
            guestTeacherModuleEntity.setId(UUID.randomUUID());
            guestTeacherModuleEntity.setModule(createModuleEntity(moduleName));
            teacherModules.add(guestTeacherModuleEntity);
        }
        teacher.setModules(teacherModules);
        return teacher;
    }


    private static GuestTeacherEntity getGuestTeacher() {
        GuestTeacherEntity teacher = new GuestTeacherEntity();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setPrefix("");
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        return teacher;
    }

    private static ModuleEntity createModuleEntity(String moduleName) {
        ModuleEntity module = new ModuleEntity();
        module.setName(moduleName);
        module.setId(UUID.randomUUID());
        module.setDeleted(false);
        return module;
    }
}