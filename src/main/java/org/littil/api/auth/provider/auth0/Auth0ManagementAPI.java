package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.id")
    String clientId;

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret")
    String clientSecret;

    @Inject
    @ConfigProperty(name = "org.littil.auth.provider_api")
    String providerApiUri;

    @Inject
    @ConfigProperty(name = "org.littil.auth.tenant_uri")
    String tenantUri;

    @Inject
    DefaultTenantConfigResolver defaultTenantConfigResolver;

    @Produces
    public ManagementAPI produceManagementAPI() throws Auth0Exception {
        String audience = getAudienceFromOidcTenantConfig();

        Auth0HttpClient auth0HttpClient = DefaultHttpClient.newBuilder().build();

        // todo if not present throw exception
        AuthAPI authAPI = AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(auth0HttpClient)
                .build();
        TokenRequest authRequest = authAPI.requestToken(audience);

        // Machine2Machine tokens is paid after 1000 tokens each month
        Response<TokenHolder> holder = authRequest.execute();

        return ManagementAPI.newBuilder(providerApiUri, holder.getBody().getAccessToken())
                .withHttpClient(auth0HttpClient)
                .build();
    }

    @NotNull
    private String getAudienceFromOidcTenantConfig() throws Auth0Exception {
        //todo improve trainwreck
        OidcTenantConfig tenantConfig = defaultTenantConfigResolver.getTenantConfigBean().getDefaultTenant().getOidcTenantConfig();

        //todo fix me
        if (tenantConfig.token.audience.isEmpty())
            throw new Auth0Exception("No audience is set to fetch a token.");

        //todo :(
        return tenantConfig.token.audience.get().get(0);  // aidoemce

    }
}
