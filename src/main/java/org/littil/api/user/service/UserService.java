package org.littil.api.user.service;


import lombok.RequiredArgsConstructor;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

    UserRepository repository;

    UserMapper mapper;

    public Optional<User> getUserById(UUID userId) {
        return repository.findByIdOptional(userId).map(mapper::toDomain);
    }
}
