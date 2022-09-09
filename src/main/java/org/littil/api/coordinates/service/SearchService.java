package org.littil.api.coordinates.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Set;

@Path("/search")
@RegisterRestClient
public interface SearchService {

    @GET
    Set<Coordinates> getCoordinatesByAdres (
            @QueryParam("postalcode") String postalcode,
            @QueryParam("street") String street,
            @QueryParam("format") String format );
}