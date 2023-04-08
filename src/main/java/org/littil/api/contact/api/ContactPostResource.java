package org.littil.api.contact.api;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
