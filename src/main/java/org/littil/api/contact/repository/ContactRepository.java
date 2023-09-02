package org.littil.api.contact.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.littil.api.auditing.repository.UserId;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<ContactEntity, UUID> {
    public Stream<ContactEntity> findByCreatedBy(final UUID userId) {
        return find("createdBy", new UserId(userId)).stream();
    }

    public Stream<ContactEntity> findByRecipientId(final UUID userId) {
        return find("recipient.id", userId).stream();
    }

    public Stream<ContactEntity> findAllByCreatedByOrRecipientId(final UUID userId) {
        return Stream.concat(
                findByCreatedBy(userId),
                findByRecipientId(userId)
        );
    }

    public Optional<ContactEntity> findByContactEntityId(final UUID id) {
        return find("id", id).firstResultOptional();
    }
}
