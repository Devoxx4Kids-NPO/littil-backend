package org.littil.api.search.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.module.service.Module;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolService;
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
    GuestTeacherService teacherService;
    
    @InjectMock
    SchoolService schoolService;

    @InjectMock
    SearchRepository searchRepository;
    

    @ParameterizedTest
    @MethodSource("provideArgumentsForWhenGetSearchResultsTest")
    public void whenGetSearchResults_ReturnExpectedSearchResult (List<School> schoolList, List<GuestTeacher> teacherList,
                  Optional<UserType> expectedUserType, List<String> expectedModules, int expectedResults) {

        List<LocationSearchResult> locationSearchResult = new ArrayList<>();
        for (School school : schoolList) {
            UUID locationId = UUID.randomUUID();
            LocationSearchResult location = new LocationSearchResult();
            location.setId(locationId);
            locationSearchResult.add(location);
            doReturn(Optional.of(school)).when(schoolService).getSchoolByLocation(locationId);
        }

        for (GuestTeacher teacher : teacherList) {
            UUID locationId = UUID.randomUUID();
            LocationSearchResult location = new LocationSearchResult();
            location.setId(locationId);
            locationSearchResult.add(location);
            doReturn(Optional.of(teacher)).when(teacherService).getTeacherByLocation(locationId);
        }

        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
                eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());
        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, expectedUserType,MAX_DISTANCE, expectedModules);
        assertNotNull(searchResults);
        assertEquals(expectedResults, searchResults.size());

        if (locationSearchResult.size() == expectedResults) {
            List<String> searchResultNames = searchResults.stream().map(SearchResult::getName).toList();
            for (School school : schoolList) {
                assertTrue(searchResultNames.contains(school.getName()));
            }
            for (GuestTeacher teacher : teacherList) {
                assertTrue(searchResultNames.contains(teacher.getFirstName() + " " + teacher.getPrefix()+ " " + teacher.getSurname()));
            }
        }
    }

    public static Stream<Arguments> provideArgumentsForWhenGetSearchResultsTest() {
        School school1 = getSchool(List.of("Scratch"));
        School school2 = getSchool(new ArrayList<>());
        List<School> allSchools = List.of(school1, school2);
        GuestTeacher teacher1 = getGuestTeacher(List.of("Scratch"));
        GuestTeacher teacher2 = getGuestTeacher(new ArrayList<>());

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

    private static School getSchool(List<String> moduleNames) {
        School school = getSchool();
        List<Module> schoolModules = new ArrayList<>();
        for (String moduleName : moduleNames) {
            Module module = new Module();
            module.setName(moduleName);
            module.setId(UUID.randomUUID());
            schoolModules.add(module);
        }
        school.setModules(schoolModules);
        return school;
    }

    private static School getSchool() {
        School school = new School();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        return school;
    }

    private static GuestTeacher getGuestTeacher(List<String> moduleNames) {
        GuestTeacher teacher = getGuestTeacher();
        List<Module> teacherModules = new ArrayList<>();
        for (String moduleName : moduleNames) {
            Module module = new Module();
            module.setName(moduleName);
            module.setId(UUID.randomUUID());
            teacherModules.add(module);
        }
        teacher.setModules(teacherModules);
        return teacher;
    }


    private static GuestTeacher getGuestTeacher() {
        GuestTeacher teacher = new GuestTeacher();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setPrefix("");
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        return teacher;
    }

}