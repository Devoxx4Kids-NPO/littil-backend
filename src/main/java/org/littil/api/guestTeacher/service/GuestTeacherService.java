package org.littil.api.guestTeacher.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auditing.repository.UserId;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleRepository;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.location.Location;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GuestTeacherService {

    private final GuestTeacherRepository repository;
    private final GuestTeacherModuleRepository moduleRepository;
    private final LocationRepository locationRepository;
    private final GuestTeacherMapper mapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationService authenticationService;
    private final TokenHelper tokenHelper;

    public Optional<GuestTeacher> getTeacherById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
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
        if (entity.getId() == null) {
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
    public void deleteGuestTeacher(@NonNull final UUID id, UUID userId) {
        Optional<GuestTeacherEntity> teacher = repository.findByIdOptional(id);
        teacher.ifPresentOrElse(teacherEntity -> {
            Optional.ofNullable(teacherEntity.getModules())
                    .orElse(new ArrayList<>())
                    .forEach(moduleRepository::delete);
            repository.delete(teacherEntity);
        }, () -> {
            throw new NotFoundException();
        });
        if (tokenHelper.getNumberOfAuthorizations() < 2) {
            userService.deleteUser(userId);
        } else {
            authenticationService.removeAuthorization(userId, AuthorizationType.GUEST_TEACHER, id);
        }
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
        GuestTeacherEntity teacher = repository.findByIdOptional(teacherId)
                .orElseThrow(() -> new NotFoundException("No teacher found for id: "+teacherId));
        if(Objects.isNull(teacher.getUser())) {
            throw new InternalServerErrorException("No user found for GuestTeacherEntity with id:" + teacherId);
        }
        return teacher.getUser().getId();
    }

    @Transactional
    public void createAndPersistDevData(UUID id, UUID userId, String email, Location location) {
        GuestTeacher teacher = new GuestTeacher();
        teacher.setAddress(location.getAddress() == null ? "Lutulistate 41" : location.getAddress());
        teacher.setPostalCode(location.getPostalCode() == null ? "6716NT" : location.getPostalCode());
        teacher.setSurname(email.split("@")[1]);
        teacher.setFirstName(email.split("@")[0]);
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
