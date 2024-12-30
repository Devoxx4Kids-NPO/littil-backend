package org.littil.api.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.littil.api.auth.provider.Provider;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;

    @NotEmpty(message = "{User.EmailAddress.required}")
    @Email
    private String emailAddress;

    private Provider provider;

    private String providerId;

    private Set<String> roles = new HashSet<>();

    private Integer loginsCount;

    private Date lastLogin;
}
