package org.littil.api.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.teacher.api.TeacherMapper;
import org.littil.api.teacher.api.TeacherUpsertResource;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    public Optional<Teacher> getTeacherByName(final String name) {
        return Optional.ofNullable(repository.findByName(name));
    }

    public Optional<Teacher> getTeacherById(final UUID id) {
        return repository.findByIdOptional(id);
    }

    @Transactional
    // todo: for example validations
    public Teacher saveTeacher(final TeacherUpsertResource resource) {
        Teacher teacher = mapper.teacherResourceToTeacher(resource);
        repository.persist(teacher);

        if( repository.isPersistent(teacher)) {
            Optional<Teacher> optionalEmp = repository.findByIdOptional(teacher.getId());
            return optionalEmp.orElseThrow(NotFoundException::new);
        } else {
            throw new PersistenceException();
        }
    }

    public List<Teacher> findAll() {
        return repository.listAll();
    }

    @Transactional
    public void deleteTeacher(UUID id) {
        Optional<Teacher> teacher = repository.findByIdOptional(id);
        teacher.ifPresentOrElse(repository::delete, () -> { throw new NotFoundException(); });
    }
}
