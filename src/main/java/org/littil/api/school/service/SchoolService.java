package org.littil.api.school.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.ServiceException;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class SchoolService {

    private final SchoolRepository repository;
    private final LocationRepository locationRepository;
    private final SchoolMapper mapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;

    public List<School> getSchoolByName(@NonNull final String name) {
        return repository.findBySchoolNameLike(name).stream().map(mapper::toDomain).toList();
    }

    public Optional<School> getSchoolById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public List<School> findAll() {
        return repository.listAll().stream().map(mapper::toDomain).toList();
    }

    @Transactional
    public School saveSchool(@Valid School school, String subject) {
        SchoolEntity entity = mapper.toEntity(school);
        // todo I don't like having to inject the user mapper and user service for this usecase.
        Optional<User> user = userService.getUserByProviderId(subject);
        if (user.isEmpty()) {
            throw new ServiceException(String.format("Unable to create school due to the fact the corresponding user with provider id %s does not exists.", subject));
        }

        UserEntity userEntity = userMapper.toEntity(user.get());
        entity.setUser(userEntity);
        locationRepository.persist(entity.getLocation());
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            authenticationService.addAuthorization(subject, AuthorizationType.SCHOOL, entity.getId());
            //todo add school role
            userService.updateUser(school);
            return mapper.updateDomainFromEntity(entity, school);
        } else {
            throw new PersistenceException();
        }
    }

    @Transactional
    public void deleteSchool(@NonNull final UUID id) {
        //todo also call userService to remove school role from the user.
        Optional<SchoolEntity> school = repository.findByIdOptional(id);
        school.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
    }

    @Transactional
    public School update(@Valid School school) {
        if (Objects.isNull(school.getId())) {
            throw new ServiceException("School does not have a Id");
        }

        SchoolEntity entity = repository.findByIdOptional(school.getId())
                .orElseThrow(() -> new NotFoundException("No School found for Id"));

        mapper.updateEntityFromDomain(school, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, school);

    }
}