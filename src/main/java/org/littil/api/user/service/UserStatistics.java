package org.littil.api.user.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserStatistics {
    private String authorizationType;
    private Long count;
    private LocalDateTime lastCreated;
}