package org.littil.api.teacher;

import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.littil.api.exception.AlreadyExistsException;
import org.littil.api.exception.NotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    public Set<TeacherDto> getTeacherByName(final String surname) {
        return repository.findBySurname(surname) //
        		.stream() //
        		.map(mapper::teacherToTeacherDto) //
        		.collect(Collectors.toSet());
    }

    public TeacherDto getTeacherById(final Long id) {
        return mapper.teacherToTeacherDto(repository.findById(id));
    }

    public Set<TeacherDto> saveTeacher(final TeacherDto teacherDto) {
        // todo: for example validations
    	if (repository.findByEmail(teacherDto.getEmail()).isPresent()) {
    		throw new AlreadyExistsException("Teacher already exists");
    	}
        repository.persist(mapper.teacherDtoToTeacher(teacherDto));
        return getAll();
    }

    public Set<TeacherDto> getAll() {
        return repository.streamAll().map(mapper::teacherToTeacherDto).collect(Collectors.toSet());
    }

    public Set<TeacherDto> deleteTeacherById(long id) {
    	if (repository.findById(id) == null) {
	    	throw new NotFoundException("Teacher not found");
	    }
  	   repository.deleteById(id);
       return getAll();
    }
}
