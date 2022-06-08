package org.littil.api;


import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.*;

import javax.ws.rs.core.Application;

@SecuritySchemes( {
        @SecurityScheme(
                securitySchemeName = "security-oauth",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(
                        implicit = @OAuthFlow(
                                authorizationUrl = "https://dev-g60bne29.eu.auth0.com/authorize",
                                tokenUrl = "https://dev-g60bne29.eu.auth0.como/auth/token",
                                scopes = {}
                        )
                )
        )
}
)
@OpenAPIDefinition(
//        security = @SecurityRequirement(name = "security-oauth"),
        info = @Info(
                title="Littil backend API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Littil",
                        url = "https://littil.org/contact",
                        email = "info@littil.org"),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
public class LittilBackendAPI extends Application {
}
