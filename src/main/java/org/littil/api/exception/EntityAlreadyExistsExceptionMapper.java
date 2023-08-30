package org.littil.api.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EntityAlreadyExistsExceptionMapper extends AbstractExceptionMapper<EntityAlreadyExistsException> {

    public EntityAlreadyExistsExceptionMapper() {
        super(Response.Status.CONFLICT);
    }
}