package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.contactPerson.repository.ContactPersonRepository;
import org.littil.api.exception.ServiceException;
import org.littil.api.location.repository.LocationEntity;
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
    private final ContactPersonRepository contactPersonRepository;
    private final SchoolMapper mapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;
    private final TokenHelper tokenHelper;

    public List<School> getSchoolByName(@NonNull final String name) {
        return repository.findBySchoolNameLike(name).stream().map(mapper::toDomain).toList();
    }

    public Optional<School> getSchoolById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public Optional<School> getSchoolByLocation(@NonNull final UUID locationId) {
        LocationEntity location = locationRepository.findById(locationId);
        return repository.findByLocation(location).map(mapper::toDomain);
    }

    public List<School> findAll() {
        return repository.listAll().stream().map(mapper::toDomain).toList();
    }

    School saveSchool(@Valid School school, UUID userId) {
        SchoolEntity entity = mapper.toEntity(school);
        // todo I don't like having to inject the user mapper and user service for this usecase.
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            throw new ServiceException(String.format("Unable to create School due to the fact the corresponding user with provider id %s does not exists.", userId));
        }
        // todo why store entire user in entity, we could also only store id. Would prevent us from using userService here
        UserEntity userEntity = userMapper.toEntity(user.get());
        entity.setUser(userEntity);
        contactPersonRepository.persist(entity.getContactPerson());
        locationRepository.persist(entity.getLocation());
        if(entity.getId()==null) {
            entity.setId(UUID.randomUUID());
        }
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            authenticationService.addAuthorization(userId, AuthorizationType.SCHOOL, entity.getId());
            return mapper.updateDomainFromEntity(entity, school);
        } else {
            throw new PersistenceException("Something went wrong when persisting School for user " + userId);
        }
    }

    @Transactional
    public School saveOrUpdate(@Valid School school, UUID userId) {
        return Objects.isNull(school.getId()) ? saveSchool(school, userId) : update(school);
    }

    @Transactional
    public void deleteSchool(@NonNull final UUID id, UUID userId) {
        Optional<SchoolEntity> school = repository.findByIdOptional(id);
        school.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
        authenticationService.removeAuthorization(userId, AuthorizationType.SCHOOL, id);
    }

    School update(@Valid School school) {
        UUID userId = tokenHelper.getCurrentUserId();
        SchoolEntity entity = repository.findByIdOptional(school.getId())
                .orElseThrow(() -> new NotFoundException("No School found for Id"));

        if (entity.getUser() != null && !entity.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Update not allowed, user is not the owner of this entity.");
        }
        mapper.updateEntityFromDomain(school, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, school);
    }

    @Transactional
    public void createAndPersistDevData(UUID id, UUID userId) {
        School school = new School();
        school.setName("Dev School");
        school.setAddress("Lutulistate 41");
        school.setPostalCode("6716NT");
        school.setSurname("Nederland");
        school.setFirstName("LITTIL");
        school.setPrefix("Devoxx4Kids");
        var entity = this.mapper.toEntity(school);
        entity.setId(id);
        this.userService.getUserById(userId).map(userMapper::toEntity).ifPresent(entity::setUser);
        this.contactPersonRepository.persist(entity.getContactPerson());
        this.locationRepository.persist(entity.getLocation());
        this.repository.persist(entity);
        log.info("persisted Dev School {} for Development purposes",id);
    }
}