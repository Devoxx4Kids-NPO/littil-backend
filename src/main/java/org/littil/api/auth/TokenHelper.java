package org.littil.api.auth;

import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.inject.Inject;

public class TokenHelper {
    @Inject
    JsonWebToken jwt;
}
