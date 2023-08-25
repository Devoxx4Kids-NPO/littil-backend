package org.littil.api.module.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ModuleServiceTest {

    @Inject
    ModuleService moduleService;

    @Test
    void giveFindAll_thenShouldReturnModuleList() {

        List<Module> modules = moduleService.findAll();

        assertThat(modules).isNotEmpty();
        assertThat(modules.stream().map(Module::getName).toList()).contains("Scratch");
    }
}