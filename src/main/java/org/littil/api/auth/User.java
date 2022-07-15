package org.littil.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.littil.api.auth.provider.Provider;
import org.littil.api.guestTeacher.service.GuestTeacher;
import org.littil.api.school.service.School;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @NonNull
    String id;

    @NotEmpty(message = "{User.firstName.required}")
    @Email
    String emailAddress;
    GuestTeacher guestTeacher;

    Provider provider = Provider.AUTH0;
    private Set<Role> roles = new HashSet<>();

    School school;
}
