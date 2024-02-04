package org.littil.api.contact.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.TokenHelper;
import org.littil.api.contact.repository.ContactEntity;
import org.littil.api.contact.repository.ContactRepository;
import org.littil.api.mail.MailService;
import org.littil.api.metrics.LittilMetrics;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    private final ContactMapper mapper;
    private final ContactRepository repository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final TokenHelper tokenHelper;
    private final MailService mailService;
    @Inject
    @ConfigProperty(name = "org.littil.contact.cc_email")
    Optional<String> ccEmail;

    @Transactional
    public Optional<Contact> saveAndSend(Contact contact) {
        ContactEntity contactEntity = mapper.toEntity(contact);
        Optional<UserEntity> recipient = userService
                .getUserById(contact.getRecipient())
                .map(userMapper::toEntity);
        if(recipient.isPresent()) {
            contactEntity.setRecipient(recipient.get());
        } else {
            log.warn("unable get recipient for {}",contact.getRecipient());
            return Optional.empty();
        }
        UUID id = UUID.randomUUID();
        contactEntity.setId(id);
        repository.persist(contactEntity);
        // send contact mail to contact recipient
        mailService.sendContactMailRecipient(contactEntity.getRecipient().getEmailAddress(),contact.getMessage(),contact.getMedium(), this.ccEmail.orElse(null));
        log.info(LittilMetrics.Contact.mailSent());
        // send contact mail to initiating user
        tokenHelper.currentUserId()
                .flatMap(this.userService::getUserById)
                .map(User::getEmailAddress)
                .ifPresent(createdByAddress -> mailService.sendContactMailInitiatingUser(createdByAddress,contact.getMessage(),contact.getMedium(),null));
        return repository.findByContactEntityId(id)
                        .map(mapper::toDomain);
    }

    public List<Contact> findAllMyContacts() {
        return tokenHelper.currentUserId()
                .stream()
                .flatMap(this.repository::findAllByCreatedByOrRecipientId)
                .map(this.mapper::toDomain)
                .toList();
    }
}
