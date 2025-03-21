package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.TokenHelper;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.module.repository.ModuleRepository;
import org.littil.api.module.service.Module;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.littil.api.school.repository.SchoolModuleRepository;
import org.littil.api.school.repository.SchoolRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class SchoolModuleService {

    private final ModuleRepository moduleRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolModuleRepository schoolModuleRepository;
    private final TokenHelper tokenHelper;
    private final SchoolModuleMapper mapper;

    /*
    *  return list of active Modules for a given school
    */
    public List<Module> getSchoolModulesBySchoolId(final UUID schoolId) {
        SchoolEntity school = getSchoolEntity(schoolId);
        return school.getModules() //
                .stream() //
                .filter(schoolModule -> ! schoolModule.getModule().getDeleted()) //
                .map(mapper::toDomain) //
                .toList();
    }

    /*
     *   update modules of given guestTeacherId
     */
    @Transactional
    public void save(@NonNull final UUID schoolId, @NonNull final List<String> modules) {
        SchoolEntity school = getSchoolEntity(schoolId);

        // add module if missing
        modules.stream()
                .map(UUID::fromString)
                .filter(moduleId -> ! isExistingModule(school, moduleId))
                .map(this::getModuleEntity)
                .map(moduleEntity -> mapToSchoolModuleEntity(school, moduleEntity))
                .forEach(schoolModuleRepository::persist);

        // remove if not in modules
        school.getModules().stream()
                .filter(entity -> ! modules.contains(entity.getModule().getId().toString()))
                .forEach(schoolModuleRepository::delete);
    }

    /*
     *   check if moduleId is existing module for the guest teacher
     */
    private boolean isExistingModule(SchoolEntity entity, UUID moduleId) {
        return entity.getModules().stream()
                .map(SchoolModuleEntity::getModule)
                .map(ModuleEntity::getId)
                .toList()
                .contains(moduleId);
    }

        /*
     *   get schoolEntity and validate user
     */
    @NotNull
    protected SchoolEntity getSchoolEntity(@NotNull UUID schoolId) {
        UUID userId = tokenHelper.getCurrentUserId();
        SchoolEntity school = schoolRepository.findByIdOptional(schoolId)
                .orElseThrow(() -> new NotFoundException("No School found for Id"));
        if ((school.getUser() == null) || !school.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Action not allowed, user is not the owner of this entity.");
        }
        if (school.getModules() == null) {
            school.setModules(new ArrayList<>());
        }
        return school;
    }

    /*
     *   get moduleEntity and validate
     */
    @NotNull
    protected ModuleEntity getModuleEntity(@NotNull UUID moduleId) {
        return moduleRepository
                .findByIdOptional(moduleId)
                .filter(m -> !Boolean.TRUE.equals(m.getDeleted()))
                .orElseThrow(() ->  new NotFoundException("Module "+moduleId+" not found or not valid"));
    }

    private static SchoolModuleEntity mapToSchoolModuleEntity(SchoolEntity school, ModuleEntity moduleEntity) {
        SchoolModuleEntity newSchoolModuleEntity = new SchoolModuleEntity();
        newSchoolModuleEntity.setId(UUID.randomUUID());
        newSchoolModuleEntity.setModule(moduleEntity);
        newSchoolModuleEntity.setSchool(school);
        return newSchoolModuleEntity;
    }
}