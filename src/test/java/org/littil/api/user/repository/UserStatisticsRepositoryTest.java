package org.littil.api.user.repository;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.service.UserStatistics;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;


@QuarkusTest
public class UserStatisticsRepositoryTest {

    @Mock
    EntityManager entityManager;

    @InjectMocks
    UserStatisticsRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserStatistics() {
        // Arrange
        Tuple userTuple = mock(Tuple.class);
        Tuple schoolTuple = mock(Tuple.class);
        Tuple guestTeacherTuple = mock(Tuple.class);

        TypedQuery<Tuple> userQuery = mock(TypedQuery.class);
        TypedQuery<Tuple> schoolQuery = mock(TypedQuery.class);
        TypedQuery<Tuple> guestTeacherQuery = mock(TypedQuery.class);

        when(entityManager.createNamedQuery("UserEntity.countAndMaxCreatedAt", Tuple.class)).thenReturn(userQuery);
        when(entityManager.createNamedQuery("SchoolEntity.countAndMaxCreatedAt", Tuple.class)).thenReturn(schoolQuery);
        when(entityManager.createNamedQuery("GuestTeacherEntity.countAndMaxCreatedAt", Tuple.class)).thenReturn(guestTeacherQuery);

        when(userQuery.getSingleResult()).thenReturn(userTuple);
        when(schoolQuery.getSingleResult()).thenReturn(schoolTuple);
        when(guestTeacherQuery.getSingleResult()).thenReturn(guestTeacherTuple);

        when(userTuple.get(0, Long.class)).thenReturn(10L);
        when(userTuple.get(1, LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 1, 1, 0, 0));
        when(schoolTuple.get(0, Long.class)).thenReturn(5L);
        when(schoolTuple.get(1, LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 1, 1, 0, 0));
        when(guestTeacherTuple.get(0, Long.class)).thenReturn(3L);
        when(guestTeacherTuple.get(1, LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 1, 1, 0, 0));

        UserStatistics expectedUserStatistics = new UserStatistics(AuthorizationType.USER.getTokenValue(), 10L, LocalDateTime.of(2023, 1, 1, 0, 0));
        UserStatistics expectedSchoolStatistics = new UserStatistics(AuthorizationType.SCHOOL.getTokenValue(), 5L, LocalDateTime.of(2023, 1, 1, 0, 0));
        UserStatistics expectedGuestTeacherStatistics = new UserStatistics(AuthorizationType.GUEST_TEACHER.getTokenValue(), 3L, LocalDateTime.of(2023, 1, 1, 0, 0));

        // Act
        List<UserStatistics> actualStatistics = repository.getUserStatistics();

        // Assert
        assertEquals(3, actualStatistics.size());
        assertTrue(actualStatistics.contains(expectedUserStatistics));
        assertTrue(actualStatistics.contains(expectedSchoolStatistics));
        assertTrue(actualStatistics.contains(expectedGuestTeacherStatistics));


    }
}
