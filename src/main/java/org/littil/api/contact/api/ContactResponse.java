package org.littil.api.contact.api;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ContactResponse {
    private UUID id;

    private UUID recipient;

    private LocalDateTime contactDate;

}
