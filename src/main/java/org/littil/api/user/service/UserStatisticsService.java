package org.littil.api.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.user.repository.*;

import java.util.List;

@ApplicationScoped
@AllArgsConstructor
@Slf4j
public class UserStatisticsService {

   UserStatisticsRepository repository;

    @Transactional
    public List<UserStatistics> getUserStatistics () {
        return repository.getUserStatistics()
                .stream()
                .toList();
    }
}
