package org.littil.api.teacher.api;

import org.littil.api.teacher.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface TeacherMapper {
    
    TeacherResource teacherToTeacherResource(Teacher teacher);

    @Mapping(target = "id", ignore = true)
    Teacher teacherResourceToTeacher(TeacherUpsertResource teacherResource);
}
