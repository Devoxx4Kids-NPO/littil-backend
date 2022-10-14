package org.littil.api.search.service;

import lombok.Builder;
import lombok.Data;
import org.littil.api.search.api.UserType;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@Builder
public class SearchResult {

    //Todo: Add messages to properties

    @NotEmpty(message = "The id is required")
    private UUID id;

    @NotEmpty(message = "The name is required")
    private String name;

    @NotEmpty(message = "The latitude is required")
    private double latitude;

    @NotEmpty(message = "The longitude is required")
    private double longitude;

    @NotEmpty(message = "The distance is required")
    private double distance;

    @NotEmpty(message = "The userType is required")
    private UserType userType;
}