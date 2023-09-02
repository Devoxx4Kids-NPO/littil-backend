package org.littil.api.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper extends AbstractExceptionMapper<AuthenticationException> {

	public AuthenticationExceptionMapper() {
		super(Response.Status.FORBIDDEN);
	}
}