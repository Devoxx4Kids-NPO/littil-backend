package org.littil.api.search.repository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.littil.api.location.LocationConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import java.util.UUID;

@NamedStoredProcedureQuery(
        name = "FindWithinRadius",
        procedureName = "FindNearest",
        resultClasses = {LocationSearchResult.class},
        parameters = {
                @StoredProcedureParameter(name = "latitude", type = Double.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "longitude", type = Double.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "startDistance", type = Integer.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "maxDistance", type = Integer.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "limit", type = Integer.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "condition", type = String.class, mode = ParameterMode.IN)
        }
)
@Data
@Entity
public class LocationSearchResult {

    @Id
    @Column(name = "location_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Getter(AccessLevel.NONE)
    @Column(name = "latitude")
    private double latitude;

    @Getter(AccessLevel.NONE)
    @Column(name = "longitude")
    private double longitude;

    @Column(name = "dist")
    private double distance;

    public double getLatitude() {
        return latitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }

    public double getLongitude() {
        return longitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }
}
