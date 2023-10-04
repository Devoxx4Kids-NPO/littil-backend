package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.function.Supplier;

@Singleton
@Slf4j
class ManagementAPISupplier implements Supplier<ManagementAPI> {
    private final Auth0ManagementTokenProvider tokenProvider;
    private TokenHolder token;
    private final ManagementAPI api;

    ManagementAPISupplier(
            @ConfigProperty(name = "org.littil.auth.provider_api") String providerApiUri,
            Auth0ManagementTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.token = this.tokenProvider.getNewToken();
        this.api = ManagementAPI.newBuilder(providerApiUri,this.token.getAccessToken()).build();
    }


    @Override
    public ManagementAPI get() {
        if(this.token.getExpiresAt().toInstant().isBefore(Instant.now())) {
            this.token = this.tokenProvider.getNewToken();
            this.api.setApiToken(getToken().getAccessToken());
        }
        return this.api;
    }

    TokenHolder getToken() {
        return this.token;
    }
}
