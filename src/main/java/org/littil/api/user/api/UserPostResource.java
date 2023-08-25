package org.littil.api.user.api;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class UserPostResource {

    @NotEmpty(message = "{User.EmailAddress.required}")
    private String emailAddress;
}
