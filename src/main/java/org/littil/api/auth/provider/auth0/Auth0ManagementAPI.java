package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {



    ManagementAPI managementAPI;

    @PostConstruct
    void init() {
        AuthAPI authAPI = new AuthAPI(tenantConfig.getTenantId().map(Object::toString).orElse(""), tenantConfig.getClientId().map(Object::toString).orElse(""), tenantConfig.getCredentials().getClientSecret().toString());
        AuthRequest authRequest = authAPI.requestToken(tenantConfig.getAuthorizationPath());

        TokenHolder holder = authRequest.execute();
        managementAPI = new ManagementAPI(DOMAIN, holder.getAccessToken());
    }
}
