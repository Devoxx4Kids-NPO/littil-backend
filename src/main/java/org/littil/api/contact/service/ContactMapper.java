package org.littil.api.contact.service;

import org.littil.api.contact.repository.ContactEntity;
import org.littil.api.contact.api.ContactPostResource;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface ContactMapper {

    Contact toDomain(ContactPostResource contactResource);

    @Mapping(source = "createdDate", target = "contactDate")
    @Mapping(source = "recipient.id", target = "recipient")
    Contact toDomain(ContactEntity contactEntity);

    @InheritInverseConfiguration(name = "toDomain")
    @Mapping(target = "id", ignore = true)
    ContactEntity toEntity(Contact contact);
}
