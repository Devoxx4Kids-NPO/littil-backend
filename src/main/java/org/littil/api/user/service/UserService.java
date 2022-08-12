package org.littil.api.user.service;


import lombok.AllArgsConstructor;
import org.littil.api.auth.service.AuthUser;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.PasswordService;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.mail.MailService;
import org.littil.api.school.service.School;
import org.littil.api.user.repository.UserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class UserService {

    AuthenticationService authenticationService;
    UserRepository repository;
    MailService mailService;
    UserMapper mapper;
    PasswordService passwordService;

    public Optional<User> getUserById(UUID userId) {
        return repository.findByIdOptional(userId).map(mapper::toDomain);
    }

    public List<User> listUsers() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional
    public void updateUser(School school) {
        //todo implement update role and store in db
    }

    @Transactional
    public void updateUser(GuestTeacher guestTeacher) {
        //todo implement update role and store in db
    }

    @Transactional
    public User createUser(User user) {
        String tempPassword = passwordService.generate();

        // todo create and set temp password
        AuthUser createdUser = authenticationService.createUser(mapper.toAuthUser(user), tempPassword);

        // todo how does it work if user already is present in db?
        repository.persist(mapper.toEntity(user));
        mailService.sendWelcomeMail(user, tempPassword);

        return user;
    }

    @Transactional
    public void deleteUser(UUID id) {
        //todo find corrensponding user and delete in repository and in auth0
//        authenticationService.deleteUser(id.toString());
    }
}
