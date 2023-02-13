package org.littil.api.module.repository;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ModuleRepositoryTest {

    @Inject
    ModuleRepository repository;

    @Test
    void givenFindAll_thenShouldReturnSuccessfully() {
        String name = "Scratch";
        List<ModuleEntity> foundModules = repository.findAll().list();
        assertThat(foundModules).isNotNull();
        assertThat(foundModules.stream().map(ModuleEntity::getName).toList()).contains(name);
    }

}