package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

	@Override
	public Response toResponse(AuthenticationException e) {
		ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(e.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(errorMessage);
		return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
	}

}