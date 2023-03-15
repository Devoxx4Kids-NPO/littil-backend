package org.littil.api.school.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.littil.api.auth.TokenHelper;
import org.littil.api.module.repository.ModuleEntity;
import org.littil.api.module.repository.ModuleRepository;
import org.littil.api.module.service.Module;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolModuleEntity;
import org.littil.api.school.repository.SchoolModuleRepository;
import org.littil.api.school.repository.SchoolRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Transactional
    public void deleteSchoolModule(@NonNull final UUID schoolId, UUID moduleId) {
        SchoolEntity school = getSchoolEntity(schoolId);
        Optional<SchoolModuleEntity> schoolModule = school.getModules().stream() //
                .filter(s -> s.getModule().getId().equals(moduleId))
                .findFirst();
        if (schoolModule.isEmpty()) {
            throw new NotFoundException("Module not found.");
        }
        SchoolModuleEntity entity = schoolModule.get();
        schoolModuleRepository.delete(entity);
    }

    @Transactional
    public void save (@NonNull final UUID schoolId, @NonNull final Module module ) {
        SchoolEntity school = getSchoolEntity(schoolId);
        ModuleEntity moduleEntity = getModuleEntity (module);
        Optional<SchoolModuleEntity> schoolModule = school.getModules().stream() //
                .filter(s -> s.getModule().getId().equals(module.getId()))
                .findFirst();
        if (schoolModule.isEmpty()) {
            SchoolModuleEntity newSchoolModuleEntity = new SchoolModuleEntity();
            newSchoolModuleEntity.setId(UUID.randomUUID());
            newSchoolModuleEntity.setModule(moduleEntity);
            newSchoolModuleEntity.setSchool(school);
            schoolModule = Optional.of(newSchoolModuleEntity);
        }
        schoolModuleRepository.persist(schoolModule.get());
    }

    /*
     *   get schoolEntity and validate user
     */
    @NotNull
    private SchoolEntity getSchoolEntity(@NotNull UUID school_id) {
        UUID userId = tokenHelper.getCurrentUserId();
        SchoolEntity school = schoolRepository.findByIdOptional(school_id)
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
    private ModuleEntity getModuleEntity(@NotNull Module module) {
        ModuleEntity moduleEntity = moduleRepository.findById(module.getId());
        if (moduleEntity == null ||
                !moduleEntity.getName().equals(module.getName()) ||
                moduleEntity.getDeleted()) {
            throw new NotFoundException("Module not found or not valid.");
        }
        return moduleEntity;
    }

}