package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityAlreadyExistsExceptionMapper extends AbstractExceptionMapper<EntityAlreadyExistsException> {

    public EntityAlreadyExistsExceptionMapper() {
        super(Response.Status.CONFLICT);
    }
}