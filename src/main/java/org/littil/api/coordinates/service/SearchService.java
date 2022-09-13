package org.littil.api.coordinates.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import java.util.Set;

@RegisterRestClient
public interface SearchService {

    @GET
    Set<Coordinates> getCoordinatesByAddress(
            @QueryParam("postalcode") String postalCode,
            @QueryParam("street") String street,
            @QueryParam("format") String format );
}