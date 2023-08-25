package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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
import static org.junit.jupiter.api.Assertions.*;
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
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school,  false);
        school.setModules(List.of(schoolModuleEntity));
        UUID expectedModuleId = schoolModuleEntity.getModule().getId();
        String expectedModuleName = schoolModuleEntity.getModule().getName();

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

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
        school.setModules(List.of(createSchoolModuleEntities(school,  true)));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

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

        List<Module>  modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

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

        List<Module>  modules = schoolModuleService.getSchoolModulesBySchoolId(schoolId);

        assertThat(modules).isEmpty();
    }

    @Test
    void givenGetSchoolModulesBySchoolId_forUnknownSchoolId_thenShouldThrowNotFoundException () {
        UUID schoolId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> schoolModuleService.getSchoolModulesBySchoolId(schoolId));
    }

    @Test
    void givenDeleteSchoolModule_thenShouldDeleteSchoolModule () {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school,  false);
        school.setModules(List.of(schoolModuleEntity));
        UUID moduleId = schoolModuleEntity.getModule().getId();

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        schoolModuleService.deleteSchoolModule(schoolId, moduleId);
        Mockito.verify(schoolModuleRepository, times(1)).delete(any());
    }

    @Test
    void givenDeleteSchoolModule_forUnknownSchoolId_thenShouldThrowNotFoundException () {
        UUID schoolId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> schoolModuleService.deleteSchoolModule(schoolId, moduleId));
        Mockito.verify(schoolModuleRepository, never()).delete(any());
    }

    @Test
    void givenDeleteSchoolModule_forUnknownSchoolModuleId_thenShouldThrowNotFoundException () {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        school.setModules(new ArrayList<>());

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.deleteSchoolModule(schoolId, moduleId));
        Mockito.verify(schoolModuleRepository, never()).persist(any(SchoolModuleEntity.class));
    }

    @Test
    void givenSaveSchoolModule_thenShouldSave() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        UUID moduleId = schoolModuleEntity.getModule().getId();
        String moduleName = schoolModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(schoolModuleEntity.getModule());
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        schoolModuleService.save(schoolId, module);

        ArgumentCaptor<SchoolModuleEntity> captor = ArgumentCaptor.forClass(SchoolModuleEntity.class);
        Mockito.verify(schoolModuleRepository, times(1)).persist(captor.capture());
        SchoolModuleEntity entity = captor.getValue();
        assertThat(entity.getModule().getDeleted()).isFalse();
    }

    @Test
    void givenSaveSchoolModule_forNullModule_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school,  false);
        school.setModules(List.of(schoolModuleEntity));
        UUID moduleId = schoolModuleEntity.getModule().getId();
        String moduleName = schoolModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(null);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, module));
        Mockito.verify(schoolModuleRepository, never()).persist(any(SchoolModuleEntity.class));
    }

    @Test
    void givenSaveSchoolModule_forModuleWithWrongName_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school,  false);
        school.setModules(List.of(schoolModuleEntity));
        UUID moduleId = schoolModuleEntity.getModule().getId();
        String moduleName = schoolModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);
        ModuleEntity repositoryModuleEntity = schoolModuleEntity.getModule();
        repositoryModuleEntity.setName(RandomStringUtils.randomAlphanumeric(10));

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(repositoryModuleEntity);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, module));
        Mockito.verify(schoolModuleRepository, never()).persist(any(SchoolModuleEntity.class));
    }

    @Test
    void givenSaveSchoolModule_forDeletedModule_thenShouldThrowNotFoundException() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SchoolEntity school = createSchoolEntity(schoolId, userId);
        SchoolModuleEntity schoolModuleEntity = createSchoolModuleEntities(school, false);
        school.setModules(List.of(schoolModuleEntity));
        UUID moduleId = schoolModuleEntity.getModule().getId();
        String moduleName = schoolModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);
        ModuleEntity repositoryModuleEntity = createSchoolModuleEntities(school,  true).getModule();

        when(schoolRepository.findByIdOptional(schoolId)).thenReturn(Optional.of(school));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(repositoryModuleEntity);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, module));
        Mockito.verify(schoolModuleRepository, never()).persist(any(SchoolModuleEntity.class));
    }

    @Test
    void givenSaveSchoolModule_forUnknownSchoolId_thenShouldThrowNotFoundException () {
        UUID schoolId = UUID.randomUUID();
        String moduleName = RandomStringUtils.randomAlphanumeric(10);
        Module module = createModule(UUID.randomUUID(), moduleName);
        assertThrows(NotFoundException.class, () -> schoolModuleService.save(schoolId, module));
        Mockito.verify(schoolModuleRepository, never()).persist(any(SchoolModuleEntity.class));
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

    private Module createModule(UUID moduleId, String moduleName) {
        Module module = new Module();
        module.setId(moduleId);
        module.setName(moduleName);
        return module;
    }
}
