package org.littil.api.coordinates.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coordinates {
    private double lat;
    private double lon;
    private String display_name;
}