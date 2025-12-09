package org.littil.api.user.api;

import jakarta.validation.constraints.Email;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class UserPostResource {

    @Email
    @NotEmpty(message = "{User.EmailAddress.required}")
    private String emailAddress;
}
