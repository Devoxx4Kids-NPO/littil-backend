package org.littil.api.coordinates.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coordinates {
    private Double lat;
    private Double lon;
    private String display_name;
}