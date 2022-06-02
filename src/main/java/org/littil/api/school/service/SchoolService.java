package org.littil.api.school.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.exception.ServiceException;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;

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
    private final SchoolMapper mapper;

    public Optional<School> getSchoolByName(@NonNull final String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    public Optional<School> getSchoolById(@NonNull final UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    public List<School> findAll() {
        return repository.listAll().stream().map(mapper::toDomain).toList();
    }

    @Transactional
    public School saveSchool(@Valid School school) {
        SchoolEntity entity = mapper.toEntity(school);
        repository.persist(entity);

        if (repository.isPersistent(entity)) {
            return mapper.updateDomainFromEntity(entity, school);
        } else {
            throw new PersistenceException();
        }
    }

    @Transactional
    public void deleteSchool(@NonNull final UUID id) {
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