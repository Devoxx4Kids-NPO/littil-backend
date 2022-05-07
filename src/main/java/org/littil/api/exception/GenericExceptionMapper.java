package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
  
  @Override
  public Response toResponse(Throwable arg0) {
    // TODO Auto-generated method stub
    return null;
  }
}
