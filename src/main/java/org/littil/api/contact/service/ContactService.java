package org.littil.api.contact.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.TokenHelper;
import org.littil.api.contact.repository.ContactEntity;
import org.littil.api.contact.repository.ContactRepository;
import org.littil.api.mail.MailService;
import org.littil.api.user.repository.UserEntity;
import org.littil.api.user.service.UserMapper;
import org.littil.api.user.service.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
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
        contactEntity.setId(UUID.randomUUID());
        repository.persist(contactEntity);
        mailService.sendContactMail(contactEntity.getRecipient().getEmailAddress(),contact.getMessage(),contact.getMedium());
        // FIXME: send email to sender
        return Optional.of(mapper.toDomain(contactEntity));
    }

    public List<Contact> findAllMyContacts() {
        return tokenHelper.currentUserId()
                .stream()
                .flatMap(this.repository::findAllByCreatedByOrRecipientId)
                .map(this.mapper::toDomain)
                .toList();
    }
}