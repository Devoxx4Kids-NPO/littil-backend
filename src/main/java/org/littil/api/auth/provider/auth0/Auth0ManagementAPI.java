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

import jakarta.inject.Singleton;
import org.littil.api.auth.provider.auth0.exception.Auth0UserException;

import java.time.Instant;
import java.util.*;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {
    private final ManagementAPI managementAPI;
    private final AuthAPI authAPI;
    final String audience;
    private Instant expiresAt;

    public Auth0ManagementAPI(
        @ConfigProperty(name = "org.littil.auth.provider_api") String providerApiUri,
        @ConfigProperty(name = "org.littil.auth.machine2machine.client.id") String clientId,
        @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret")  String clientSecret,
        @ConfigProperty(name = "org.littil.auth.tenant_uri") String tenantUri,
        DefaultTenantConfigResolver defaultTenantConfigResolver
    ) {
        Auth0HttpClient http = DefaultHttpClient.newBuilder().build();
        this.authAPI = AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(http)
                .build();
        this.managementAPI = ManagementAPI.newBuilder(providerApiUri, "empty")
                .withHttpClient(http)
                .build();
        this.audience = getAudienceFromOidcTenantConfig(defaultTenantConfigResolver)
                .orElseThrow(() -> new Auth0UserException("No audience is set to fetch a token."));
    }

    boolean tokenIsExpired() {
        return this.expiresAt==null || this.expiresAt.isBefore(Instant.now());
    }

    public ManagementAPI getManagementAPI() {
        if (tokenIsExpired()) {
            updateAccessToken();
        }
        return this.managementAPI;
    }

    TokenRequest createRequestForToken() {
        return this.authAPI.requestToken(this.audience);
    }

    private void updateAccessToken() {
        log.info("### getNewAccessToken");
        TokenRequest authRequest = createRequestForToken();
        try {
            // Machine2Machine tokens is paid after 1000 tokens each month
            var token = authRequest.execute().getBody();
            this.expiresAt = token.getExpiresAt().toInstant();
            log.info("### token expires at {}", this.expiresAt);
            this.managementAPI.setApiToken(token.getAccessToken());
        } catch(Auth0Exception e) {
            log.error("unable to get new access token for {}",audience,e);
        }
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
