package org.littil.api.school.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class SchoolModuleRepository implements PanacheRepositoryBase<SchoolModuleEntity, UUID> {

}
