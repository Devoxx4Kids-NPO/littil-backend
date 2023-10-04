package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.UsersEntity;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;

/**
 * manages ManagementAPI instance and it's apiToken, refreshes when required
 */
@Singleton
@Slf4j
class ManagementAPISupplier {
    private final Auth0ManagementTokenProvider tokenProvider;
    private Instant expiresAt;
    private final ManagementAPI api;

    ManagementAPISupplier(
            @ConfigProperty(name = "org.littil.auth.provider_api") String providerApiUri,
            Auth0ManagementTokenProvider tokenProvider) {
        this.api = ManagementAPI.newBuilder(providerApiUri,"").build();
        this.tokenProvider = tokenProvider;
        updateWithNewToken();
    }

    boolean tokenIsExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

    private void updateWithNewToken() {
        var token = this.tokenProvider.getNewToken();
        this.expiresAt = token.getExpiresAt().toInstant();
        this.api.setApiToken(token.getAccessToken());
    }

    private ManagementAPI api() {
        if(tokenIsExpired()) {
            updateWithNewToken();
        }
        return this.api;
    }

    public UsersEntity users() {
        return api().users();
    }

    public RolesEntity roles() {
        return api().roles();
    }
}
