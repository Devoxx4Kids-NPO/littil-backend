package org.littil.api.auth.provider.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
//https://github.com/auth0/auth0-java#api-clients-recommendations
public class Auth0ManagementAPI {

    @ConfigProperty(name = "org.littil.auth.machine2machine.client.id")
    private String clientId;
    @ConfigProperty(name = "org.littil.auth.machine2machine.client.secret")
    private String clientSecret;
    @Inject
    DefaultTenantConfigResolver defaultTenantConfigResolver;

    @Produces
    public ManagementAPI produceManagementAPI() throws Auth0Exception {
        //todo improve trainwreck
        OidcTenantConfig tenantConfig = defaultTenantConfigResolver.getTenantConfigBean()
                .getDefaultTenant()
                .getOidcTenantConfig();
        // todo if not present throw exception
        AuthAPI authAPI = new AuthAPI(tenantConfig.getTenantId().get() + ".eu.auth0.com", //todo make uri configurable?
                clientId,
                clientSecret);
        // todo fix audience url
        AuthRequest authRequest = authAPI.requestToken("https://dev-g60bne29.eu.auth0.com/api/v2/");

        // Machine2Machine tokens is paid after 1000 tokens each month
        TokenHolder holder = authRequest.execute();
        // todo fix domain with proper config
        return new ManagementAPI(tenantConfig.getTenantId().get() + ".eu.auth0.com", holder.getAccessToken());
    }
}
