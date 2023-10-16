package org.littil.api.guestTeacher.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.TokenHelper;
import org.littil.api.guestTeacher.repository.GuestTeacherEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleEntity;
import org.littil.api.guestTeacher.repository.GuestTeacherModuleRepository;
import org.littil.api.guestTeacher.repository.GuestTeacherRepository;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.module.repository.ModuleRepository;
import org.littil.api.module.service.Module;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GuestTeacherModuleService {

    private final ModuleRepository moduleRepository;
    private final GuestTeacherRepository guestTeacherRepository;
    private final GuestTeacherModuleRepository guestTeacherModuleRepository;
    private final TokenHelper tokenHelper;
    private final GuestTeacherModuleMapper mapper;

    /*
    *  return list of active Modules for a given guestTeacher
    */
    public List<Module> getGuestTeacherModulesByGuestTeacherId(final UUID guestTeacherId) {
        GuestTeacherEntity guestTeacher = getGuestTeacherEntity(guestTeacherId);
        return guestTeacher.getModules() //
                .stream() //
                .filter(guestTeacherModule -> ! guestTeacherModule.getModule().getDeleted()) //
                .map(mapper::toDomain) //
                .toList();
    }

    @Transactional
    public void deleteGuestTeacherModule(@NonNull final UUID guestTeacherId, UUID moduleId) {
        GuestTeacherEntity guestTeacher = getGuestTeacherEntity(guestTeacherId);
        Optional<GuestTeacherModuleEntity> guestTeacherModule = guestTeacher.getModules().stream() //
                .filter(s -> s.getModule().getId().equals(moduleId))
                .findFirst();
        if (guestTeacherModule.isEmpty()) {
            throw new NotFoundException("Module not found.");
        }
        GuestTeacherModuleEntity entity = guestTeacherModule.get();
        guestTeacherModuleRepository.delete(entity);
    }

    @Transactional
    public void save (@NonNull final UUID guestTeacherId, @NonNull final Module module ) {
        GuestTeacherEntity guestTeacher = getGuestTeacherEntity(guestTeacherId);
        ModuleEntity moduleEntity = getModuleEntity (module.getId());
        Optional<GuestTeacherModuleEntity> guestTeacherModule = guestTeacher.getModules().stream() //
                .filter(s -> s.getModule().getId().equals(module.getId()))
                .findFirst();
        if (guestTeacherModule.isEmpty()) {
            GuestTeacherModuleEntity newGuestTeacherModuleEntity = mapToGuestTeacherModuleEntity(guestTeacher, moduleEntity);
            guestTeacherModule = Optional.of(newGuestTeacherModuleEntity);
        }
        guestTeacherModuleRepository.persist(guestTeacherModule.get());
    }

    /*
     *   update modules of given guestTeacherId
     */
    @Transactional
    public void save (@NonNull final UUID guestTeacherId, @NonNull final List<String> modules ) {
        GuestTeacherEntity guestTeacher = getGuestTeacherEntity(guestTeacherId);

        // add module if missing
        modules.stream()
                .map(UUID::fromString)
                .filter(moduleId -> ! isExistingModule(guestTeacher, moduleId))
                .map(this::getModuleEntity)
                .map(moduleEntity -> mapToGuestTeacherModuleEntity(guestTeacher, moduleEntity))
                .forEach(guestTeacherModuleRepository::persist);

        // remove if not in modules
        guestTeacher.getModules().stream()
                .filter(entity -> ! modules.contains(entity.getModule().getId().toString()))
                .forEach(guestTeacherModuleRepository::delete);
    }

    /*
     *   check if moduleId is existing module for the guest teacher
     */
    private boolean isExistingModule(GuestTeacherEntity entity, UUID moduleId) {
        return entity.getModules().stream()
                .map(GuestTeacherModuleEntity::getModule)
                .map(ModuleEntity::getId)
                .toList()
                .contains(moduleId);
    }

    /*
     *   get guestTeacherEntity and validate user
     */
    @NotNull
    protected GuestTeacherEntity getGuestTeacherEntity(@NotNull UUID guestTeacher_id) {
        UUID userId = tokenHelper.getCurrentUserId();
        GuestTeacherEntity guestTeacher = guestTeacherRepository.findByIdOptional(guestTeacher_id)
                .orElseThrow(() -> new NotFoundException("No GuestTeacher found for Id"));
        if ((guestTeacher.getUser() == null) || !guestTeacher.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Action not allowed, user is not the owner of this entity.");
        }
        if (guestTeacher.getModules() == null) {
            guestTeacher.setModules(new ArrayList<>());
        }
        return guestTeacher;
    }

    /*
     *   get moduleEntity and validate
     */
    @NotNull
    protected ModuleEntity getModuleEntity(@NotNull UUID moduleId) {
        ModuleEntity moduleEntity = moduleRepository.findById(moduleId);
        if (moduleEntity == null ||
                moduleEntity.getDeleted()) {
            throw new NotFoundException("Module not found or not valid.");
        }
        return moduleEntity;
    }

    private static GuestTeacherModuleEntity mapToGuestTeacherModuleEntity(GuestTeacherEntity guestTeacher, ModuleEntity moduleEntity) {
        GuestTeacherModuleEntity entity = new GuestTeacherModuleEntity();
        entity.setId(UUID.randomUUID());
        entity.setModule(moduleEntity);
        entity.setGuestTeacher(guestTeacher);
        return entity;
    }
}