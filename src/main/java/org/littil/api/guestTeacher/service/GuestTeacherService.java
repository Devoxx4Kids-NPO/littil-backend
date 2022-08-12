package org.littil.api.guestTeacher.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.exception.ServiceException;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;

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
public class GuestTeacherService {

    private final GuestTeacherRepository repository;
    private final GuestTeacherMapper mapper;

    public List<GuestTeacher> getTeacherByName(@NonNull final String name) {
        return repository.findByName(name).stream().map(mapper::toDomain).toList();
    }

    public Optional<GuestTeacher> getTeacherById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public List<GuestTeacher> findAll() {
        return repository.listAll().stream().map(mapper::toDomain).toList();
    }

    @Transactional
    public GuestTeacher saveTeacher(@Valid GuestTeacher guestTeacher) {
        GuestTeacherEntity entity = mapper.toEntity(guestTeacher);
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            //todo call userService to add school role to user. (first refactor user/school/guestTeacher entity
            return mapper.updateDomainFromEntity(entity, guestTeacher);
        } else {
            throw new PersistenceException();
        }
    }

    @Transactional
    public void deleteTeacher(@NonNull final UUID id) {
        //todo also call userService to remove school role from the user.
        Optional<GuestTeacherEntity> teacher = repository.findByIdOptional(id);
        teacher.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
    }

    @Transactional
    public GuestTeacher update(@Valid GuestTeacher guestTeacher) {
        if (Objects.isNull(guestTeacher.getId())) {
            throw new ServiceException("Teacher does not have a Id");
        }

        GuestTeacherEntity entity = repository.findByIdOptional(guestTeacher.getId())
                .orElseThrow(() -> new NotFoundException("No Teacher found for Id"));

        mapper.updateEntityFromDomain(guestTeacher, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, guestTeacher);

    }
}
