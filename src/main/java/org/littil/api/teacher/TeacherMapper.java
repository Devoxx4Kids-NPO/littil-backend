package org.littil.api.teacher;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface TeacherMapper {
    
    TeacherDto teacherToTeacherDto(Teacher teacher);

    @Mapping(target = "id", ignore = true)
    Teacher teacherDtoToTeacher(TeacherDto teacherDto);
}
