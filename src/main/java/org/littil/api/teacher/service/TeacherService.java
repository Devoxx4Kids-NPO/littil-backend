package org.littil.api.teacher.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.exception.ServiceException;
import org.littil.api.teacher.repository.TeacherEntity;
import org.littil.api.teacher.repository.TeacherRepository;

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
public class TeacherService {

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    public Optional<Teacher> getTeacherByName(@NonNull final String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    public Optional<Teacher> getTeacherById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public List<Teacher> findAll() {
        return mapper.toDomainList(repository.listAll());
    }

    @Transactional
    public Teacher saveTeacher(@Valid Teacher teacher) {
        TeacherEntity entity = mapper.toEntity(teacher);
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            return mapper.updateDomainFromEntity(entity, teacher);
        } else {
            throw new PersistenceException();
        }
    }

    @Transactional
    public void deleteTeacher(@NonNull final UUID id) {
        Optional<TeacherEntity> teacher = repository.findByIdOptional(id);
        teacher.ifPresentOrElse(repository::delete, () -> {
            throw new NotFoundException();
        });
    }

    @Transactional
    public Teacher update(@Valid Teacher teacher) {
        if (Objects.isNull(teacher.getId())) {
            throw new ServiceException("Teacher does not have a Id");
        }

        TeacherEntity entity = repository.findByIdOptional(teacher.getId())
                .orElseThrow(() -> new NotFoundException("No Teacher found for Id"));

        mapper.updateEntityFromDomain(teacher, entity);
        repository.persist(entity);
        return mapper.updateDomainFromEntity(entity, teacher);

    }
}
