package org.littil.api.user.service;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.api.user.repository.UserStatisticsRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class UserStatisticsServiceTest {

    @Mock
    UserStatisticsRepository repository;

    @InjectMocks
    UserStatisticsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    public void testGetUserStatistics() {
        // Arrange
        UserStatistics stat1 = new UserStatistics("role1", 2L, LocalDateTime.now());
        UserStatistics stat2 = new UserStatistics("role2", 1L, LocalDateTime.now());
        List<UserStatistics> expectedStatistics = Arrays.asList(stat1, stat2);

        when(repository.getUserStatistics()).thenReturn(expectedStatistics);

        // Act
        List<UserStatistics> actualStatistics = service.getUserStatistics();

        // Assert
        assertEquals(expectedStatistics, actualStatistics);
    }
}
