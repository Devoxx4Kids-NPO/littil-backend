package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper extends AbstractExceptionMapper<AuthenticationException> {

	public AuthenticationExceptionMapper() {
		super(Response.Status.FORBIDDEN);
	}
}