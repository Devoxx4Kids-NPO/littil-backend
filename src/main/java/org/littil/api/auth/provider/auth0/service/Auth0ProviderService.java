package org.littil.api.auth.provider.auth0.service;

import org.littil.api.auth.provider.Provider;
import org.littil.api.auth.service.ProviderService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Auth0ProviderService implements ProviderService {
    @Override
    public Provider whoAmI() {
        return Provider.AUTH0;
    }
}
