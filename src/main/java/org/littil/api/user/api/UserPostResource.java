package org.littil.api.user.api;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UserPostResource {

    @NotEmpty(message = "{User.EmailAddress.required}")
    private String emailAddress;
}
