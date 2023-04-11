package org.littil.api.contact.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<ContactEntity, UUID> {
    public Stream<ContactEntity> findByCreatedBy(final UUID id) {
        return find("created_by", id).stream();
    }

    public Stream<ContactEntity> findByRecipientId(final UUID id) {
        return find("recipient.id", id).stream();
    }
}
