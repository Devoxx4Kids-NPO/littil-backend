package org.littil.api.guestTeacher.service;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.provider.Provider;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleRepository;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.module.repository.ModuleRepository;
import org.littil.api.module.service.Module;
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
public class GuestTeacherModuleServiceTest {

    @Inject
    GuestTeacherModuleService guestTeacherModuleService;

    @InjectMock
    GuestTeacherRepository guestTeacherRepository;

    @InjectMock
    GuestTeacherModuleRepository guestTeacherModuleRepository;

    @InjectMock
    ModuleRepository moduleRepository;

    @InjectMock
    TokenHelper tokenHelper;

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_thenShouldReturnModules() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher,  false);
        guestTeacher.setModules(List.of(guestTeacherModuleEntity));
        UUID expectedModuleId = guestTeacherModuleEntity.getModule().getId();
        String expectedModuleName = guestTeacherModuleEntity.getModule().getName();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId);

        assertThat(modules).isNotEmpty();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0).getId()).isEqualTo(expectedModuleId);
        assertThat(modules.get(0).getName()).isEqualTo(expectedModuleName);

    }

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_withMissingUser_thenShouldReturnUnauthorized() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        guestTeacher.setUser(null);

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(UnauthorizedException.class, () -> guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId));
    }

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_withWrongUserId_thenShouldReturnUnauthorized() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(UnauthorizedException.class, () -> guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId));
    }

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_withModuleDeleted_thenShouldReturnEmptyList() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        guestTeacher.setModules(List.of(createGuestTeacherModuleEntities(guestTeacher,  true)));

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId);

        assertThat(modules).isEmpty();
    }


    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_withGuestTeacherModuleEntitiesNull_thenShouldReturnEmptyList() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        guestTeacher.setModules(null);

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId);

        assertThat(modules).isEmpty();
    }

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_withGuestTeacherModuleEntitiesEmptyList_thenShouldReturnEmptyList() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        guestTeacher.setModules(new ArrayList<>());

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        List<Module>  modules = guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId);

        assertThat(modules).isEmpty();
    }

    @Test
    void givenGetGuestTeacherModulesByGuestTeacherId_forUnknownGuestTeacherId_thenShouldThrowNotFoundException () {
        UUID guestTeacherId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.getGuestTeacherModulesByGuestTeacherId(guestTeacherId));
    }

    @Test
    void givenDeleteGuestTeacherModule_thenShouldDeleteGuestTeacherModule () {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher,  false);
        guestTeacher.setModules(List.of(guestTeacherModuleEntity));
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        guestTeacherModuleService.deleteGuestTeacherModule(guestTeacherId, moduleId);
        Mockito.verify(guestTeacherModuleRepository, times(1)).delete(any());
    }

    @Test
    void givenDeleteGuestTeacherModule_forUnknownGuestTeacherId_thenShouldThrowNotFoundException () {
        UUID guestTeacherId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.deleteGuestTeacherModule(guestTeacherId, moduleId));
        Mockito.verify(guestTeacherModuleRepository, never()).delete(any());
    }

    @Test
    void givenDeleteGuestTeacherModule_forUnknownGuestTeacherModuleId_thenShouldThrowNotFoundException () {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        guestTeacher.setModules(new ArrayList<>());

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.deleteGuestTeacherModule(guestTeacherId, moduleId));
        Mockito.verify(guestTeacherModuleRepository, never()).persist(any(GuestTeacherModuleEntity.class));
    }

    @Test
    void givenSaveGuestTeacherModule_thenShouldSave() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();
        String moduleName = guestTeacherModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(guestTeacherModuleEntity.getModule());
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        guestTeacherModuleService.save(guestTeacherId, module);

        ArgumentCaptor<GuestTeacherModuleEntity> captor = ArgumentCaptor.forClass(GuestTeacherModuleEntity.class);
        Mockito.verify(guestTeacherModuleRepository, times(1)).persist(captor.capture());
        GuestTeacherModuleEntity entity = captor.getValue();
        assertThat(entity.getModule().getDeleted()).isFalse();
    }

    @Test
    void givenSaveGuestTeacherModule_forNullModule_thenShouldThrowNotFoundException() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher,  false);
        guestTeacher.setModules(List.of(guestTeacherModuleEntity));
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();
        String moduleName = guestTeacherModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(null);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, module));
        Mockito.verify(guestTeacherModuleRepository, never()).persist(any(GuestTeacherModuleEntity.class));
    }

    @Test
    void givenSaveGuestTeacherModule_forModuleWithWrongName_thenShouldThrowNotFoundException() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher,  false);
        guestTeacher.setModules(List.of(guestTeacherModuleEntity));
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();
        String moduleName = guestTeacherModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);
        ModuleEntity repositoryModuleEntity = guestTeacherModuleEntity.getModule();
        repositoryModuleEntity.setName(RandomStringUtils.randomAlphanumeric(10));

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(repositoryModuleEntity);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, module));
        Mockito.verify(guestTeacherModuleRepository, never()).persist(any(GuestTeacherModuleEntity.class));
    }

    @Test
    void givenSaveGuestTeacherModule_forDeletedModule_thenShouldThrowNotFoundException() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);
        guestTeacher.setModules(List.of(guestTeacherModuleEntity));
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();
        String moduleName = guestTeacherModuleEntity.getModule().getName();
        Module module = createModule(moduleId, moduleName);
        ModuleEntity repositoryModuleEntity = createGuestTeacherModuleEntities(guestTeacher,  true).getModule();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findById(any(UUID.class))).thenReturn(repositoryModuleEntity);
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, module));
        Mockito.verify(guestTeacherModuleRepository, never()).persist(any(GuestTeacherModuleEntity.class));
    }

    @Test
    void givenSaveGuestTeacherModule_forUnknownGuestTeacherId_thenShouldThrowNotFoundException () {
        UUID guestTeacherId = UUID.randomUUID();
        String moduleName = RandomStringUtils.randomAlphanumeric(10);
        Module module = createModule(UUID.randomUUID(), moduleName);
        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, module));
        Mockito.verify(guestTeacherModuleRepository, never()).persist(any(GuestTeacherModuleEntity.class));
    }

    private GuestTeacherEntity createGuestTeacherEntity(UUID guestTeacherId, UUID UserId) {
        GuestTeacherEntity guestTeacherEntity = new GuestTeacherEntity();
        guestTeacherEntity.setId(guestTeacherId);
        guestTeacherEntity.setSurname(RandomStringUtils.randomAlphabetic(10));
        UserEntity user = new UserEntity(UserId, Provider.AUTH0, "providerId", "email@littil.org");
        guestTeacherEntity.setUser(user);
        return guestTeacherEntity;
    }

    private GuestTeacherModuleEntity createGuestTeacherModuleEntities(GuestTeacherEntity guestTeacher, boolean ModuleDeleted) {
        GuestTeacherModuleEntity entity = new GuestTeacherModuleEntity();
        entity.setId(UUID.randomUUID());
        entity.setGuestTeacher(guestTeacher);
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
