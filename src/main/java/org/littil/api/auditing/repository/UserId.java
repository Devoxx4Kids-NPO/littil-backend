package org.littil.api.auditing.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embeddable;
import java.util.UUID;


@Data
@AllArgsConstructor
@Embeddable
public class UserId {
    private UUID id;
}