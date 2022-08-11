package org.littil.api.user.service;

import lombok.Data;
import org.littil.api.auth.Role;
import org.littil.api.auth.provider.Provider;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.school.service.School;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class User {
    private UUID id;

    @NotEmpty(message = "{User.EmailAddress.required}")
    @Email
    private String emailAddress;

    private Provider authProvider;

    private GuestTeacher guestTeacher;

    private School school;

    private Set<Role> roles = new HashSet<>();
}
