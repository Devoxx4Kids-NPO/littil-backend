package org.littil.api.coordinates.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import java.util.Set;

@RegisterRestClient
public interface SearchService {

    @GET
    Set<Coordinates> getCoordinatesByAddress(
            @QueryParam("q") String query,
            @QueryParam("format") String format);
}