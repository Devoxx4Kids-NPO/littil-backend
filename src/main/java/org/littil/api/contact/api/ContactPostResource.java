package org.littil.api.contact.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ContactPostResource {

    @NotNull(message = "{Contact.recipient.required}")
    private UUID recipient;

    @NotEmpty(message = "{Contact.medium.required}")
    private String medium;

    @NotEmpty(message = "{Contact.message.required}")
    @Size(max = 1000, message = "{Contact.message.max}")
    private String message;
}
