package org.littil.api.auth.service;

import org.littil.api.school.service.School;
import org.littil.api.teacher.service.Teacher;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {
    public Teacher registrate(TeacherUser teacher) {
        //implement registration stuff
        return new Teacher();
    }

    public School registrate(SchoolUser school) {
        //implement registration stuff
        return new School();
    }
}
