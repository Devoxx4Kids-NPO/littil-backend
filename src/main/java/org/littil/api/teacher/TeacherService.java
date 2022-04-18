package org.littil.api.teacher;

import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    public TeacherDto getTeacherByName(final String name) {
        return mapper.teacherToTeacherDto(repository.findByName(name));
    }

    public TeacherDto getTeacherById(final Long id) {
        return mapper.teacherToTeacherDto(repository.findById(id));
    }

    public Set<TeacherDto> saveTeacher(final TeacherDto teacherDto) {
        // todo: for example validations

        repository.persist(mapper.teacherDtoToTeacher(teacherDto));
        return getAll();
    }

    public Set<TeacherDto> getAll() {
        return repository.streamAll().map(mapper::teacherToTeacherDto).collect(Collectors.toSet());
    }

    public Set<TeacherDto> deleteTeacher(TeacherDto teacherDto) {
        // todo: for example check if exists

        repository.delete(mapper.teacherDtoToTeacher(teacherDto));
        return getAll();
    }
}