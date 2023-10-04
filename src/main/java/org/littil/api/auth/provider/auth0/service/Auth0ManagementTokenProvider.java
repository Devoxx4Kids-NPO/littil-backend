package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.TokenProvider;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.quarkus.oidc.runtime.TenantConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementTokenProvider implements TokenProvider {


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
        // FIXME: this tokenHolder must refresh after expiry(holder.body.expiresAt)
        return ManagementAPI.newBuilder(providerApiUri, holder.getBody().getAccessToken())
                .withHttpClient(auth0HttpClient)
                .build();
    }

    private String getAudienceFromOidcTenantConfig() throws Auth0Exception {
        List<String> audience = Optional.ofNullable(defaultTenantConfigResolver)
                .map(DefaultTenantConfigResolver::getTenantConfigBean)
                .map(TenantConfigBean::getDefaultTenant)
                .map(TenantConfigContext::getOidcTenantConfig)
                .map(OidcTenantConfig::getToken)
                .map(OidcTenantConfig.Token::getAudience)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(new ArrayList<>());

        if (audience.isEmpty())
            throw new Auth0Exception("No audience is set to fetch a token.");

        return audience.get(0);

    }

    @Override
    public String getToken() throws Auth0Exception {
        return getNewToken().getAccessToken();
    }

    @Override
    public CompletableFuture<String> getTokenAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getToken();
            } catch (Auth0Exception e) {
                throw new RuntimeException("unable to get new token",e);
            }
        });
    }

    private TokenHolder getNewToken() throws Auth0Exception {
        Auth0HttpClient auth0HttpClient = DefaultHttpClient.newBuilder().build();

        // todo if not present throw exception
        var audience = getAudienceFromOidcTenantConfig();
        var request = AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(auth0HttpClient)
                .build()
                .requestToken(audience);

        // Machine2Machine tokens is paid after 1000 tokens each month
        log.info("getNewToken machine2machine token for audience {}",audience);
        var response = request.execute();
        log.info("token response status {}",response.getStatusCode());
        return response.getBody();
    }
}
