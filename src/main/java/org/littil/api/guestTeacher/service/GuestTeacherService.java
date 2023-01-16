package org.littil.api.guestTeacher.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auditing.repository.UserId;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.time.DayOfWeek;
import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GuestTeacherService {

    private final GuestTeacherRepository repository;
    private final LocationRepository locationRepository;
    private final GuestTeacherMapper mapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;

    public Optional<GuestTeacher> getTeacherById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public Optional<GuestTeacher> getTeacherByLocation(@NonNull final UUID locationId) {
        LocationEntity location = locationRepository.findById(locationId);
        return repository.findByLocation(location).map(mapper::toDomain);
    }

    public List<GuestTeacher> findAll() {
        return repository.listAll().stream().map(mapper::toPurgedDomain).toList();
    }

    @Transactional
    public GuestTeacher saveOrUpdate(@Valid GuestTeacher guestTeacher, UUID userId) {
        return Objects.isNull(guestTeacher.getId()) ? saveTeacher(guestTeacher, userId) : update(guestTeacher, userId);
    }

    GuestTeacher saveTeacher(@Valid GuestTeacher guestTeacher, UUID userId) {
        GuestTeacherEntity entity = mapper.toEntity(guestTeacher);
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            throw new ServiceException(String.format("Unable to create GuestTeacher due to the fact the corresponding user with provider id %s does not exists.", userId));
        }

        // todo why store entire user in entity, we could also only store id. Would prevent us from using userService here
        UserEntity userEntity = userMapper.toEntity(user.get());
        entity.setUser(userEntity);
        locationRepository.persist(entity.getLocation());
        if(entity.getId()==null) {
            entity.setId(UUID.randomUUID());
        }
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            authenticationService.addAuthorization(userId, AuthorizationType.GUEST_TEACHER, entity.getId());
            return mapper.updateDomainFromEntity(entity, guestTeacher);
        } else {
            throw new PersistenceException("Something went wrong when persisting GuestTeacher for user " + userId);
        }
    }

    @Transactional
    public void deleteTeacher(@NonNull final UUID id, UUID userId) {
        Optional<GuestTeacherEntity> teacher = repository.findByIdOptional(id);
        teacher.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
        authenticationService.removeAuthorization(userId, AuthorizationType.GUEST_TEACHER, id);
    }

    GuestTeacher update(@Valid GuestTeacher guestTeacher, UUID userId) {
        GuestTeacherEntity entity = repository.findByIdOptional(guestTeacher.getId())
                .orElseThrow(() -> new NotFoundException("No Teacher found for Id"));

        if (entity.getUser() != null && !entity.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Update not allowed, user is not the owner of this entity.");
        }
        mapper.updateEntityFromDomain(guestTeacher, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, guestTeacher);
    }

    public UUID getUserIdByTeacherId(@NonNull final UUID teacherId) {
        Optional<GuestTeacherEntity> teacherOptional = repository.findByIdOptional(teacherId);
        if(teacherOptional.isEmpty()) {
            throw new NotFoundException();
        }
        UserEntity user = teacherOptional.get().getUser();
        if(Objects.isNull(user)) {
            throw new InternalServerErrorException("No user found for GuestTeacherEntity with id:" + teacherId);
        }
        return user.getId();
    }

    @Transactional
    public void createAndPersistDevData(UUID id, UUID userId) {
        GuestTeacher teacher = new GuestTeacher();
        teacher.setAddress("Lutulistate 41");
        teacher.setPostalCode("6716NT");
        teacher.setSurname("Nederland");
        teacher.setFirstName("LITTIL");
        teacher.setPrefix("Devoxx4Kids");
        teacher.setAvailability(EnumSet.of(DayOfWeek.TUESDAY,DayOfWeek.THURSDAY));
        var entity = this.mapper.toEntity(teacher);
        entity.setId(id);
        entity.setCreatedBy(new UserId(userId));
        this.userService.getUserById(userId).map(userMapper::toEntity).ifPresent(entity::setUser);
        this.locationRepository.persist(entity.getLocation());
        this.repository.persist(entity);
        log.info("persisted Dev GuestTeacher {} for Development purposes",id);
    }
}
