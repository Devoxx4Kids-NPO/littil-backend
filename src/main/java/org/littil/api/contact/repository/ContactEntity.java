package org.littil.api.contact.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;
import org.littil.api.user.repository.UserEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Contact")
@Table(name = "contact")
public class ContactEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "user_id")
    private UserEntity recipient;
}
