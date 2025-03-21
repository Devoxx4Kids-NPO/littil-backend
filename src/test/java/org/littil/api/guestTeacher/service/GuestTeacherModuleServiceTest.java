package org.littil.api.guestTeacher.service;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.littil.RandomStringGenerator;
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
class GuestTeacherModuleServiceTest {

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

        assertThat(modules).isNotEmpty().hasSize(1);
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
    void givenSaveGuestTeacherModules_withNewModule_thenShouldSave() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();

        assertThat(guestTeacher.getModules()).isNullOrEmpty();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(guestTeacherModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        guestTeacherModuleService.save(guestTeacherId, List.of(moduleId.toString()));

        ArgumentCaptor<GuestTeacherModuleEntity> captor = ArgumentCaptor.forClass(GuestTeacherModuleEntity.class);
        Mockito.verify(guestTeacherModuleRepository, times(1)).persist(captor.capture());
        GuestTeacherModuleEntity entity = captor.getValue();
        assertThat(entity.getModule().getDeleted()).isFalse();
        Mockito.verify(guestTeacherModuleRepository, times(0)).delete(any());
   }

    @Test
    void givenSaveGuestTeacherModules_ForDeletedModule_thenShouldThrowNotFoundException() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();
        ModuleEntity moduleEntity = guestTeacherModuleEntity.getModule();
        moduleEntity.setDeleted(Boolean.TRUE);

        assertThat(guestTeacher.getModules()).isNullOrEmpty();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(moduleEntity));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, List.of(moduleId.toString())));
    }

    @Test
    void givenSaveGuestTeacherModules_ForUnknownModule_thenShouldThrowNotFoundException() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        UUID moduleId = UUID.randomUUID();

        assertThat(guestTeacher.getModules()).isNullOrEmpty();

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.empty());
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        assertThrows(NotFoundException.class, () -> guestTeacherModuleService.save(guestTeacherId, List.of(moduleId.toString())));
    }

    @Test
    void givenSaveGuestTeacherModules_withExistingModule_thenShouldDoNothing() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);
        UUID moduleId = guestTeacherModuleEntity.getModule().getId();

        guestTeacher.setModules(List.of(guestTeacherModuleEntity));

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(guestTeacherModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        guestTeacherModuleService.save(guestTeacherId, List.of(moduleId.toString()));

        Mockito.verify(guestTeacherModuleRepository, times(0)).persist(any(GuestTeacherModuleEntity.class));
        Mockito.verify(guestTeacherModuleRepository, times(0)).delete(any());
    }

    @Test
    void givenSaveGuestTeacherModules_withEmptyList_thenShouldDeleteModule() {
        UUID guestTeacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GuestTeacherEntity guestTeacher = createGuestTeacherEntity(guestTeacherId, userId);
        GuestTeacherModuleEntity guestTeacherModuleEntity = createGuestTeacherModuleEntities(guestTeacher, false);

        guestTeacher.setModules(List.of(guestTeacherModuleEntity));

        when(guestTeacherRepository.findByIdOptional(guestTeacherId)).thenReturn(Optional.of(guestTeacher));
        when(moduleRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(guestTeacherModuleEntity.getModule()));
        when(tokenHelper.getCurrentUserId()).thenReturn(userId);

        guestTeacherModuleService.save(guestTeacherId, new ArrayList<>());

        Mockito.verify(guestTeacherModuleRepository, times(0)).persist(any(GuestTeacherModuleEntity.class));
        Mockito.verify(guestTeacherModuleRepository, times(1)).delete(any());
    }

    private GuestTeacherEntity createGuestTeacherEntity(UUID guestTeacherId, UUID UserId) {
        GuestTeacherEntity guestTeacherEntity = new GuestTeacherEntity();
        guestTeacherEntity.setId(guestTeacherId);
        guestTeacherEntity.setSurname(RandomStringGenerator.generate(10));
        UserEntity user = new UserEntity(UserId, Provider.AUTH0, "providerId", "email@littil.org");
        guestTeacherEntity.setUser(user);
        return guestTeacherEntity;
    }

    private GuestTeacherModuleEntity createGuestTeacherModuleEntities(GuestTeacherEntity guestTeacher, boolean moduleDeleted) {
        GuestTeacherModuleEntity entity = new GuestTeacherModuleEntity();
        entity.setId(UUID.randomUUID());
        entity.setGuestTeacher(guestTeacher);
        entity.setModule(new ModuleEntity(UUID.randomUUID(), "moduleName", moduleDeleted));
        return entity;
    }
}
