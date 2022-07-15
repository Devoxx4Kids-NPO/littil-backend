package org.littil.api.location;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Data
public class Location {
    private UUID id;

    @NotEmpty(message = "{Location.country.required}")
    private String country;
    @NotEmpty(message = "{Location.address.required}")
    private String address;
    @NotEmpty(message = "{Location.postalCode.required}")
    private String postalCode;
    private String longitude;
    private String latitude;
}
