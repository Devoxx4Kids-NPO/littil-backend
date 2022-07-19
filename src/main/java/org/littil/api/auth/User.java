package org.littil.api.auth;

import lombok.Data;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.school.service.School;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private String id;

    @NotEmpty(message = "{User.EmailAddress.required}")
    @Email
    private String emailAddress;

    private GuestTeacher guestTeacher;

    private School school;

    private Set<Role> roles = new HashSet<>();
}
