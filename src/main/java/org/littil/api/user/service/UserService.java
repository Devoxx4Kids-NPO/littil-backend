package org.littil.api.user.service;


import lombok.RequiredArgsConstructor;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.mail.MailService;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

    AuthenticationService authenticationService;
    UserRepository repository;
    MailService mailService;

    UserMapper mapper;

    public Optional<User> getUserById(UUID userId) {
        return repository.findByIdOptional(userId).map(mapper::toDomain);
    }

    public List<User> listUsers() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    public User createUser(User user) {
        String tempPassword = UUID.randomUUID().toString().replace("-", "");
        // todo create and set temp password
        AuthUser createdUser = authenticationService.createUser(mapper.toAuthUser(user), tempPassword);

        // todo how does it work if user already is present in db?
        repository.persist(mapper.toEntity(user));
        // todo mail temp password
        mailService.sendWelcomeMail(user, tempPassword);

        return user;
    }

    public void deleteUser(String id) {
        authenticationService.deleteUser(id);
    }
}
