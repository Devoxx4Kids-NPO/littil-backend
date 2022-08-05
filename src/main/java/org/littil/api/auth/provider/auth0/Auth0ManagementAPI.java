package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import io.quarkus.oidc.OidcTenantConfig;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {
    @Inject
    OidcTenantConfig tenantConfig;

    ManagementAPI managementAPI;

    @PostConstruct
    void init() throws Auth0Exception {
        log.info("got tenant {} ", tenantConfig);
        AuthAPI authAPI = new AuthAPI(tenantConfig.getTenantId().map(Object::toString).orElse(""), tenantConfig.getClientId().map(Object::toString).orElse(""), tenantConfig.getCredentials().getClientSecret().toString());
        AuthRequest authRequest = authAPI.requestToken(tenantConfig.getAuthorizationPath().get());

        TokenHolder holder = authRequest.execute();
        // todo check if this is the proper domain
        managementAPI = new ManagementAPI(tenantConfig.getAuthServerUrl().get(), holder.getAccessToken());
    }
}
