package org.littil.api.teacher;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.littil.api.teacher.repository.TeacherEntity;
import org.littil.api.teacher.repository.TeacherRepository;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

@QuarkusTest
@Disabled("Wilko help me :(")
class TeacherRepositoryTest {

    @InjectMock
    TeacherRepository repository;

    @Test
    void findByName() {
        String surname = "John";
        Optional<TeacherEntity> teacher = Optional.of(new TeacherEntity(UUID.randomUUID(), surname, "Doe", null, null, null, null, null));

        PanacheQuery<TeacherEntity> query = Mockito.mock(PanacheQuery.class);
        Mockito.when(query.page(Mockito.any())).thenReturn(query);
        Mockito.when(query.firstResultOptional()).thenReturn(teacher);

        Mockito.when(repository.find("surname", surname)).thenReturn(query);

        Optional<TeacherEntity> foundTeacher = repository.findByName(surname);

        Assertions.assertSame(teacher, foundTeacher);
        Assertions.assertEquals(Optional.empty(), repository.findByName("non-existing-teacher"));
    }
}