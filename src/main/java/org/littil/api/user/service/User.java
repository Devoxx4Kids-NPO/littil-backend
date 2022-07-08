package org.littil.api.user.service;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public abstract class User {
    @NotEmpty(message = "{User.firstName.required}")
    @Email
    private String email;
}
