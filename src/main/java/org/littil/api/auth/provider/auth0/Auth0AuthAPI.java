package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0AuthAPI {

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.id")
    String clientId;

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret")
    String clientSecret;

//    @Inject
//    @ConfigProperty(name = "org.littil.auth.provider_api")
//    String providerApiUri;

    @Inject
    @ConfigProperty(name = "org.littil.auth.tenant_uri")
    String tenantUri;

    @Inject
    DefaultTenantConfigResolver defaultTenantConfigResolver;

    @Produces
    public AuthAPI produceAuthAPI() throws Auth0Exception {
//        String audience = getAudienceFromOidcTenantConfig();

        Auth0HttpClient auth0HttpClient = DefaultHttpClient.newBuilder().build();

        // todo if not present throw exception
//        AuthAPI authAPI =
        return AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(auth0HttpClient)
                .build();
//        TokenRequest authRequest = authAPI.requestToken(audience);
//
//        // Machine2Machine tokens is paid after 1000 tokens each month
//        Response<TokenHolder> holder = authRequest.execute();
//
//        return ManagementAPI.newBuilder(providerApiUri, holder.getBody().getAccessToken())
//                .withHttpClient(auth0HttpClient)
//                .build();
    }

//    private String getAudienceFromOidcTenantConfig() throws Auth0Exception {
//        List<String> audience = Optional.ofNullable(defaultTenantConfigResolver)
//                .map(DefaultTenantConfigResolver::getTenantConfigBean)
//                .map(TenantConfigBean::getDefaultTenant)
//                .map(TenantConfigContext::getOidcTenantConfig)
//                .map(OidcTenantConfig::getToken)
//                .map(OidcTenantConfig.Token::getAudience)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .orElse(new ArrayList<>());
//
//        if (audience.isEmpty())
//            throw new Auth0Exception("No audience is set to fetch a token.");
//
//        return audience.get(0);
//
//    }
}
