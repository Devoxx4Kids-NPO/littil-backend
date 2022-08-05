package org.littil.api.auth.provider.auth0;

import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.runtime.DefaultTenantConfigResolver;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Slf4j
public class OidcTenantConfigProducer {
    @Inject
    DefaultTenantConfigResolver defaultTenantConfigResolver;
    @Produces
    public OidcTenantConfig provide(){
        log.info("defaultTenantResolver {}", defaultTenantConfigResolver);
        OidcTenantConfig oidcTenantConfig = defaultTenantConfigResolver.getTenantConfigBean().getDefaultTenant().getOidcTenantConfig();
        log.info("providing tenantconfig {}", oidcTenantConfig);
        return oidcTenantConfig;
    }
}
