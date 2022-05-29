package org.littil.api.teacher;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TeacherServiceTest {

    @Inject
    TeacherService teacherService;

    @Test
    void whenGetAll_thenItShouldBeEmpty() {
        List<Teacher> result = teacherService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}