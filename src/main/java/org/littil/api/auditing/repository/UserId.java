package org.littil.api.auditing.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserId {
    private UUID id;
}