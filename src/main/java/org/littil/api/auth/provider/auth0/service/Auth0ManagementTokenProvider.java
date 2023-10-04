package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.client.DefaultHttpClient;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import io.quarkus.oidc.runtime.TenantConfigBean;
import io.quarkus.oidc.runtime.TenantConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Singleton;

import java.util.*;
import java.util.stream.Stream;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
class Auth0ManagementTokenProvider {
    private final AuthAPI authAPI;
    private final DefaultTenantConfigResolver defaultTenantConfigResolver;
    Auth0ManagementTokenProvider(
        @ConfigProperty(name = "org.littil.auth.machine2machine.client.id") String clientId,
        @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret") String clientSecret,
        @ConfigProperty(name = "org.littil.auth.tenant_uri") String tenantUri,
        DefaultTenantConfigResolver defaultTenantConfigResolver
    ) {
        this.authAPI = AuthAPI.newBuilder(tenantUri, clientId, clientSecret)
                .withHttpClient(DefaultHttpClient.newBuilder().build())
                .build();
        this.defaultTenantConfigResolver = defaultTenantConfigResolver;
    }

    Optional<String> getAudienceFromOidcTenantConfig() {
        return Optional.ofNullable(this.defaultTenantConfigResolver)
                .map(DefaultTenantConfigResolver::getTenantConfigBean)
                .map(TenantConfigBean::getDefaultTenant)
                .map(TenantConfigContext::getOidcTenantConfig)
                .map(OidcTenantConfig::getToken)
                .flatMap(OidcTenantConfig.Token::getAudience)
                .map(List::stream)
                .flatMap(Stream::findFirst);

    }

    TokenHolder getNewToken() {
        var audience = getAudienceFromOidcTenantConfig();
        log.info("getNewToken machine2machine token for audience {}",audience);
        try {
            var request = this.authAPI.requestToken(audience.orElseThrow());
            // Machine2Machine tokens is paid after 1000 tokens each month
            var response = request.execute();
            log.info("token response status {}",response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("error getting new machine2machine token",e);
        }
    }
}
