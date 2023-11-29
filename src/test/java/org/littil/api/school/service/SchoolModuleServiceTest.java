package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.provider.Provider;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.module.repository.ModuleRepository;
import org.littil.api.module.service.Module;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.littil.api.school.repository.SchoolModuleRepository;
import org.littil.api.school.repository.SchoolRepository;
import org.littil.api.user.repository.UserEntity;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class SchoolModuleServiceTest {

    @Inject
    SchoolModuleService schoolModuleService;

    @InjectMock
    SchoolRepository schoolRepository;

    @InjectMock
    SchoolModuleRepository schoolModuleRepository;

    @InjectMock
    ModuleRepository moduleRepository;

    @InjectMock
    TokenHelper tokenHelper;

    @Test
    void givenGetSchoolModulesBySchoolId_thenShouldReturnModules() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        school.setModules(List.of(schoolModuleEntity));
        UUID expectedModuleId = schoolModuleEntity.getModule().getId();
        String expectedModuleName = schoolModuleEntity.getModule().getName();

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module> modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

        assertThat(modules).isNotEmpty();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0).getId()).isEqualTo(expectedModuleId);
        assertThat(modules.get(0).getName()).isEqualTo(expectedModuleName);

    }

    @Test
    void givenGetSchoolModulesBySchoolId_withMissingUser_thenShouldReturnUnauthorized() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        school.setUser(null);

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(UnauthorizedException.class, () -> schoolModuleService.getSchoolModulesBySchoolId(schoolId));
    }

    @Test
    void givenGetSchoolModulesBySchoolId_withWrongUserId_thenShouldReturnUnauthorized() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(UnauthorizedException.class, () -> schoolModuleService.getSchoolModulesBySchoolId(schoolId));
    }

    @Test
    void givenGetSchoolModulesBySchoolId_withModuleDeleted_thenShouldReturnEmptyList() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        school.setModules(List.of(createSchoolModuleEntities(school, true)));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module> modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

        assertThat(modules).isEmpty();
    }


    @Test
    void givenGetSchoolModulesBySchoolId_withSchoolModuleEntitiesNull_thenShouldReturnEmptyList() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        school.setModules(null);

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module> modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

        assertThat(modules).isEmpty();
    }

    @Test
    void givenGetSchoolModulesBySchoolId_withSchoolModuleEntitiesEmptyList_thenShouldReturnEmptyList() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        school.setModules(new ArrayList<>());

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module> modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

        assertThat(modules).isEmpty();
    }

    @Test
    void givenGetSchoolModulesBySchoolId_forUnknownSchoolId_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> schoolModuleService.getSchoolModulesBySchoolId(schoolId));
    }

    @Test
    void givenSaveSchoolModules_withNewModule_thenShouldSave() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        UUID moduleId = schoolModuleEntity.getModule().getId();

        assertThat(school.getModules()).isNullOrEmpty();

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(schoolModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        schoolModuleService.save(schoolId, List.of(moduleId.toString()));

        ArgumentCaptor<SchoolModuleEntity> captor = ArgumentCaptor.forClass(SchoolModuleEntity.class);
        Mockito.verify(schoolModuleRepository, times(1)).persist(captor.capture());
        SchoolModuleEntity entity = captor.getValue();
        assertThat(entity.getModule().getDeleted()).isFalse();
        Mockito.verify(schoolModuleRepository, times(0)).delete(any());
    }

    @Test
    void givenSaveSchoolModules_withExistingModule_thenShouldDoNothing() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        UUID moduleId = schoolModuleEntity.getModule().getId();

        school.setModules(List.of(schoolModuleEntity));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(schoolModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        schoolModuleService.save(schoolId, List.of(moduleId.toString()));

        Mockito.verify(schoolModuleRepository, times(0)).persist(any(SchoolModuleEntity.class));
        Mockito.verify(schoolModuleRepository, times(0)).delete(any());
    }

    @Test
    void givenSaveSchoolModules_withEmptyList_thenShouldDeleteModule() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);

        school.setModules(List.of(schoolModuleEntity));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(schoolModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        schoolModuleService.save(schoolId, new ArrayList<>());

        Mockito.verify(schoolModuleRepository, times(0)).persist(any(SchoolModuleEntity.class));
        Mockito.verify(schoolModuleRepository, times(1)).delete(any());
    }

    @Test
    void givenSaveSchoolModules_ForDeletedModule_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        UUID moduleId = UUID.randomUUID();
        ModuleEntity moduleEntity = schoolModuleEntity.getModule();
        moduleEntity.setDeleted(Boolean.TRUE);

        school.setModules(List.of(schoolModuleEntity));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(moduleEntity));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, List.of(moduleId.toString())));
    }

    @Test
    void givenSaveSchoolModules_ForUnknownModule_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        UUID moduleId = UUID.randomUUID();

        ModuleEntity moduleEntity = schoolModuleEntity.getModule();
        moduleEntity.setDeleted(Boolean.TRUE);

        school.setModules(List.of(schoolModuleEntity));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(moduleEntity));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, List.of(moduleId.toString())));
    }

    private SchoolEntity createSchoolEntity(UUID schoolId, UUID UserId) {
        SchoolEntity schoolEntity = new SchoolEntity();
        schoolEntity.setId(schoolId);
        schoolEntity.setName(RandomStringUtils.randomAlphabetic(10));
        UserEntity user = new UserEntity(UserId, Provider.AUTH0, "providerId", "email@littil.org");
        schoolEntity.setUser(user);
        return schoolEntity;
    }

    private SchoolModuleEntity createSchoolModuleEntities(SchoolEntity school, boolean ModuleDeleted) {
        SchoolModuleEntity entity = new SchoolModuleEntity();
        entity.setId(UUID.randomUUID());
        entity.setSchool(school);
        entity.setModule(new ModuleEntity(UUID.randomUUID(), "moduleName", ModuleDeleted));
        return entity;
    }
}
