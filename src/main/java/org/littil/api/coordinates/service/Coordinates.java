package org.littil.api.coordinates.service;

import lombok.Data;

@Data
public class Coordinates {
    private Double lat;
    private Double lon;
    private String display_name;
}