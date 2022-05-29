package org.littil.api.school;

import lombok.RequiredArgsConstructor;
import org.littil.api.school.api.SchoolMapper;
import org.littil.api.school.api.SchoolUpsertResource;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@ApplicationScoped
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository repository;
    private final SchoolMapper mapper;

    public Optional<School> getSchoolByName(final String name) {
        return Optional.ofNullable(repository.findByName(name));
    }

    public Optional<School> getSchoolById(final UUID id) {
        return repository.findByIdOptional(id);
    }

    @Transactional
    // todo: for example validations
    public School saveSchool(final SchoolUpsertResource resource) {
        School school = mapper.schoolResourceToSchool(resource);
        repository.persist(school);

        if( repository.isPersistent(school)) {
            Optional<School> optionalEmp = repository.findByIdOptional(school.getId());
            return optionalEmp.orElseThrow(NotFoundException::new);
        } else {
            throw new PersistenceException();
        }
    }

    public List<School> findAll() {
        return repository.listAll();
    }

    @Transactional
    public void deleteSchool(UUID id) {
        Optional<School> school = repository.findByIdOptional(id);
        school.ifPresentOrElse(repository::delete, () -> { throw new NotFoundException(); });
    }
}
