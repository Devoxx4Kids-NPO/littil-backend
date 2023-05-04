package org.littil.api.contactPerson.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ContactPersonRepository implements PanacheRepositoryBase<ContactPersonEntity, UUID> {
}
