package org.littil.api.auth.service;

import org.littil.api.auth.provider.Provider;

public interface ProviderService {
    Provider whoAmI();
}
