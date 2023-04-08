package org.littil.api.contact.service;

import io.quarkus.security.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auditing.repository.UserId;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthenticationService;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.contact.repository.ContactEntity;
import org.littil.api.contact.repository.ContactRepository;
import org.littil.api.contactPerson.repository.ContactPersonRepository;
import org.littil.api.exception.ServiceException;
import org.littil.api.location.repository.LocationEntity;
import org.littil.api.location.repository.LocationRepository;
import org.littil.api.school.repository.SchoolEntity;
import org.littil.api.school.repository.SchoolRepository;
import org.littil.api.school.service.School;
import org.littil.api.school.service.SchoolMapper;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    private final ContactMapper mapper;
    private final ContactRepository repository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenHelper tokenHelper;

    public Contact sendAndSave(Contact contact) {
        ContactEntity contactEntity = mapper.toEntity(contact);
        userService.getUserById(contact.getRecipient())
                .map(userMapper::toEntity)
                .ifPresent(contactEntity::setRecipient);

        // FIXME: send email to recipient
        // FIXME: send email to sender
        // set createdBy? -> probably quarkus
        // set createdOn? -> probably quarkus
        repository.persist(contactEntity);
        return mapper.toDomain(contactEntity);
    }

    public Optional<Contact> getContactBy(UUID id) {
        Optional<ContactEntity> contactEntity = repository.findByIdOptional(id);

        UUID currentUserId = this.tokenHelper.getCurrentUserId();
        contactEntity
                .filter(contact -> Stream.of(contact.getCreatedBy().getId(),contact.getRecipient().getId()).noneMatch(currentUserId::equals))
                .orElseThrow(() -> new UnauthorizedException("Get not allowed, user is not the owner of this entity."));
        return contactEntity.map(mapper::toDomain);
    }

    public List<Contact> findAll() {
        UUID userId = tokenHelper.getCurrentUserId();
        return Stream.concat(
                this.repository.findByCreatedBy(userId),
                this.repository.findByRecipientId(userId)
        ).map(this.mapper::toDomain)
                .toList();
    }
}