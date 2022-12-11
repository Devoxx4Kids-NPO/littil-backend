package org.littil.api.search.repository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.littil.api.location.LocationConstants;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@SqlResultSetMapping(
        name="LocationSearchResultMapping",
        entities={
                @EntityResult(
                        entityClass = LocationSearchResult.class,
                        fields={
                                @FieldResult(name="id", column="location_id"),
                                @FieldResult(name="country", column="country_code"),
                                @FieldResult(name="address", column="address"),
                                @FieldResult(name="postalCode", column="postal_code"),
                                @FieldResult(name="latitude", column="latitude"),
                                @FieldResult(name="longitude", column="longitude"),
                                @FieldResult(name="createdBy", column="created_by"),
                                @FieldResult(name="createdDate", column="created_date"),
                                @FieldResult(name="lastModifiedBy", column="last_modified_by"),
                                @FieldResult(name="lastModifiedDate", column="last_modified_date"),
                                @FieldResult(name="distance", column="dist"),
                        }
                )
        }
)
@Data
@Entity
public class LocationSearchResult {

    @Id
    private UUID id;

    private String country;

    private String address;

    private String postalCode;

    @Getter(AccessLevel.NONE)
    private double latitude;

    @Getter(AccessLevel.NONE)
    private double longitude;

    private UUID createdBy;

    private LocalDateTime createdDate;

    private UUID lastModifiedBy;

    private LocalDateTime lastModifiedDate;

    private double distance;

    public double getLatitude() {
        return latitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }

    public double getLongitude() {
        return longitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }
}
