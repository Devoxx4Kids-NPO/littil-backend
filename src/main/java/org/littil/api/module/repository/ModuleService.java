package org.littil.api.module.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.module.service.Module;
import org.littil.api.module.service.ModuleMapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ModuleService {

    private final ModuleRepository repository;

    private final ModuleMapper mapper;

    /*
     * return a list of all active modules
     */
    public List<Module> findAll() {
        return repository.findAll() //
                .stream() //
                .filter(module -> module.getDeleted().equals(Boolean.FALSE)) //
                .map(mapper::toDomain) //
                .toList();
    }
}