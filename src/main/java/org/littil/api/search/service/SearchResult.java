package org.littil.api.search.service;

import lombok.Builder;
import lombok.Data;
import org.littil.api.search.api.UserType;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
@Builder
public class SearchResult {

    @NotEmpty(message = "{SearchResult.id.required}")
    private UUID id;

    @NotEmpty(message = "{SearchResult.userId.required}")
    private UUID userId;

    @NotEmpty(message = "{SearchResult.name.required}")
    private String name;

    @NotEmpty(message = "{SearchResult.latitude.required}")
    private double latitude;

    @NotEmpty(message = "{SearchResult.longitude.required}")
    private double longitude;

    @NotEmpty(message = "{SearchResult.distance.required}")
    private double distance;

    @NotEmpty(message = "{SearchResult.userType.required}")
    private UserType userType;
}