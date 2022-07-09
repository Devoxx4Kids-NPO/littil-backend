package org.littil.api.auth;

import io.quarkus.oidc.OidcTenantConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.littil.api.auth.provider.Provider;
import org.littil.api.school.service.School;
import org.littil.api.teacher.service.Teacher;

import javax.persistence.Embeddable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @NonNull
    String id;

    @NonNull
    String emailAddress;

    OidcTenantConfig.Roles roles;

    Provider provider = Provider.AUTH0;

    Teacher teacher;

    School school;
}
