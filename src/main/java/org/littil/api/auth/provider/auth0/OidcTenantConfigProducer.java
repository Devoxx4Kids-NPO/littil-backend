package org.littil.api.auth.provider.auth0;

import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Produces;

@Slf4j
public class OidcTenantConfigProducer {
    @Produces
    public OidcTenantConfig provide(){
        OidcTenantConfig oidcTenantConfig = new DefaultTenantConfigResolver().getTenantConfigBean().getDefaultTenant().getOidcTenantConfig();
        log.info("providing tenantconfig {}", oidcTenantConfig);
        return oidcTenantConfig;
    }
}
