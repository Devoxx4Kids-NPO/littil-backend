package org.littil.api.module.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ModuleRepository implements PanacheRepositoryBase<ModuleEntity, UUID> {


}
