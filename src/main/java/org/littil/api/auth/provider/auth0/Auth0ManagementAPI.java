package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.quarkus.oidc.runtime.TenantConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.littil.api.auth.provider.auth0.exception.Auth0UserException;

import java.time.Instant;
import java.util.*;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {
    private final ManagementAPI managementAPI;
    final String audience;
    private Instant expiresAt;

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.id")
    String clientId;

    @Inject
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret")
    String clientSecret;

    @Inject
    @ConfigProperty(name = "org.littil.auth.tenant_uri")
    String tenantUri;

    public Auth0ManagementAPI(
        @ConfigProperty(name = "org.littil.auth.provider_api") String providerApiUri,
        DefaultTenantConfigResolver defaultTenantConfigResolver
    ) {
        this.managementAPI = ManagementAPI.newBuilder(providerApiUri, "empty").build();
        this.audience = getAudienceFromOidcTenantConfig(defaultTenantConfigResolver)
                .orElseThrow(() -> new Auth0UserException("No audience is set to fetch a token."));
    }

    boolean tokenIsExpired() {
        return this.expiresAt==null || this.expiresAt.isBefore(Instant.now());
    }

    public ManagementAPI getManagementAPI() throws Auth0Exception {
        if (tokenIsExpired()) {
            TokenHolder tokenHolder = produceTokenHolder();
            this.expiresAt = tokenHolder.getExpiresAt().toInstant();
            log.info("### token expires at {}", this.expiresAt);
            this.managementAPI.setApiToken(tokenHolder.getAccessToken());
        }
        return this.managementAPI;
    }

    private TokenHolder produceTokenHolder() throws Auth0Exception {
        log.info("### produceTokenHolder");

        Auth0HttpClient auth0HttpClient = DefaultHttpClient.newBuilder().build();

        // todo if not present throw exception
        AuthAPI authAPI = AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(auth0HttpClient)
                .build();
        TokenRequest authRequest = authAPI.requestToken(audience);

        // Machine2Machine tokens is paid after 1000 tokens each month
        return authRequest.execute().getBody();
    }

    private static Optional<String> getAudienceFromOidcTenantConfig(DefaultTenantConfigResolver defaultTenantConfigResolver) {
        List<String> audience = Optional.ofNullable(defaultTenantConfigResolver)
                .map(DefaultTenantConfigResolver::getTenantConfigBean)
                .map(TenantConfigBean::getDefaultTenant)
                .map(TenantConfigContext::getOidcTenantConfig)
                .map(OidcTenantConfig::getToken)
                .flatMap(OidcTenantConfig.Token::getAudience)
                .orElseGet(Collections::emptyList);
        return audience.stream().findFirst();
    }
}
