package org.littil.api.contact.service;

import lombok.Data;
import org.littil.api.module.service.Module;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Contact {
    private UUID id;

    private UUID recipient;

    private LocalDateTime contactDate;

    private String medium;

    private String message;
}
