package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.littil.api.ErrorResponse;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
  @Override
  public Response toResponse(NotFoundException ex) {
    ErrorResponse response = new ErrorResponse(ex.getMessage());
    return Response.ok().entity(response).build();
  }
}
