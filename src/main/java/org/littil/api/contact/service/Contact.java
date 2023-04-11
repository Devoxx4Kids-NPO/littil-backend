package org.littil.api.contact.service;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Contact {
    private UUID id;

    private UUID recipient;

    private LocalDateTime contactDate;

    private String medium;

    private String message;
}
