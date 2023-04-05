package org.littil.api.search.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.guestTeacher.service.GuestTeacherService;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolService;
import org.littil.api.search.api.UserType;
import org.littil.api.search.repository.LocationSearchResult;
import org.littil.api.search.repository.SearchRepository;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
class SearchServiceTest {

    @Inject
    SearchService searchService;
    
    @InjectMock
    GuestTeacherService teacherService;
    
    @InjectMock
    SchoolService schoolService;

    @InjectMock
    SearchRepository searchRepository;
    
    @Test
    void whenGetSearchResults_withUserTypeSchool_thenReturnSearchResult () {
        
        LocationSearchResult location = new LocationSearchResult();
        List<LocationSearchResult> locationSearchResult = List.of(location);
        
        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
                eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());

        School school = new School();
        school.setName(RandomStringUtils.randomAlphabetic(10));
        doReturn(Optional.of(school)).when(schoolService).getSchoolByLocation(any());
    
        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, Optional.of(UserType.SCHOOL), new ArrayList<>());
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        assertEquals(school.getName(), searchResults.get(0).getName());
        assertEquals(0.0, searchResults.get(0).getDistance());
    }

    @Test
    void whenGetSearchResults_withUserTypeSchool_andSchoolNotFound_thenReturnEmptyList () {
        
        LocationSearchResult location = new LocationSearchResult();
        List<LocationSearchResult> locationSearchResult = List.of(location);
        
        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
                eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());
        
        doReturn(Optional.empty()).when(schoolService).getSchoolByLocation(any());
    
        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, Optional.of(UserType.SCHOOL), new ArrayList<>());
        assertNotNull(searchResults);
        assertTrue(searchResults.isEmpty());
    }

    @Test
    void whenGetSearchResults_withUserTypeQuestTeacher_thenReturnSearchResult () {

        LocationSearchResult location = new LocationSearchResult();
        List<LocationSearchResult> locationSearchResult = List.of(location);

        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
            eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());

        GuestTeacher teacher = new GuestTeacher();
        teacher.setFirstName(RandomStringUtils.randomAlphabetic(10));
        teacher.setPrefix("");
        teacher.setSurname(RandomStringUtils.randomAlphabetic(10));
        doReturn(Optional.of(teacher)).when(teacherService).getTeacherByLocation(any());

        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, Optional.of(UserType.GUEST_TEACHER), new ArrayList<>());
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        assertEquals(1, searchResults.size());
        String exptectedName = teacher.getFirstName() + " "+ teacher.getPrefix() + " "+ teacher.getSurname();
        assertEquals(exptectedName, searchResults.get(0).getName());
        assertEquals(0.0, searchResults.get(0).getDistance());
    }

    @Test
    void whenGetSearchResults_withUserTypeTeacher_andTeacherNotFound_thenReturnEmptyList () {

        LocationSearchResult location = new LocationSearchResult();
        List<LocationSearchResult> locationSearchResult = List.of(location);

        doReturn(locationSearchResult).when(searchRepository).findLocationsOrderedByDistance(
            eq(0.0), eq(0.0), anyInt(), anyInt(), anyInt(), anyString());

        doReturn(Optional.empty()).when(teacherService).getTeacherByLocation(any());

        List<SearchResult> searchResults = searchService.getSearchResults(0.0, 0.0, Optional.of(UserType.GUEST_TEACHER), new ArrayList<>());
        assertNotNull(searchResults);
        assertTrue(searchResults.isEmpty());
    }

}