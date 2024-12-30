package org.littil.api.user.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.jetbrains.annotations.NotNull;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.service.UserStatistics;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class UserStatisticsRepository {

    @Inject
    EntityManager entityManager;

    public List<UserStatistics> getUserStatistics() {
        UserStatistics userStatistics = getStatisticsForUsers();
        UserStatistics schoolStatistics = getStatisticsForSchools();
        UserStatistics guestTeacherStatistics = getStatisticsForGuestTeachers();
        return List.of(userStatistics, schoolStatistics,guestTeacherStatistics);
    }

    private UserStatistics getStatisticsForUsers() {
        Tuple result = entityManager
                .createNamedQuery("UserEntity.countAndMaxCreatedAt", Tuple.class)
                .getSingleResult();
        return mapToUserStatistics(AuthorizationType.USER, result);
    }

    private UserStatistics getStatisticsForSchools() {
        Tuple result = entityManager
                .createNamedQuery("SchoolEntity.countAndMaxCreatedAt", Tuple.class)
                .getSingleResult();
        return mapToUserStatistics(AuthorizationType.SCHOOL, result);
    }

    private UserStatistics getStatisticsForGuestTeachers() {
        Tuple result = entityManager
                .createNamedQuery("GuestTeacherEntity.countAndMaxCreatedAt", Tuple.class)
                .getSingleResult();
        return mapToUserStatistics(AuthorizationType.GUEST_TEACHER, result);
    }

    @NotNull
    private static UserStatistics mapToUserStatistics(AuthorizationType authorizationType,Tuple result) {
        Long count = result.get(0, Long.class);
        LocalDateTime maxCreatedAt = result.get(1, LocalDateTime.class);
        return new UserStatistics(authorizationType.getTokenValue(), count, maxCreatedAt);
    }
}
