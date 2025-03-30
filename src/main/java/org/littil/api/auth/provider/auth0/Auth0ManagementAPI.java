package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.quarkus.oidc.runtime.TenantConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Singleton;
import org.littil.api.auth.provider.auth0.exception.Auth0UserException;

import java.time.Duration;
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
    private final RateLimiter rateLimiter;

    Auth0ManagementAPI(
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

        // request to auth0 management api  are limited to 2 requests per second
        // see https://auth0.com/docs/troubleshoot/customer-support/operational-policies/rate-limit-policy/rate-limit-configurations/free-public
        RateLimiterConfig config = getRateLimiterConfig();
        this.rateLimiter = RateLimiter.of("auth0RateLimiter", config);
    }

    private static RateLimiterConfig getRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(1000))
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(2)
                .build();
    }

    public UsersEntity users() { return getManagementAPI().users(); }

    public RolesEntity roles() { return getManagementAPI().roles(); }

    boolean tokenIsExpired() {
        return this.expiresAt==null || this.expiresAt.isBefore(Instant.now());
    }

    ManagementAPI getManagementAPI() {
        RateLimiter.waitForPermission(rateLimiter);
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
                .map(OidcTenantConfig::token)
                .flatMap(OidcTenantConfig.Token::audience)
                .orElseGet(Collections::emptyList);
        return audience.stream().findFirst();
    }
}
