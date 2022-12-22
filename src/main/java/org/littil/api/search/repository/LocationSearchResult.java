package org.littil.api.search.repository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.littil.api.location.LocationConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.SqlResultSetMapping;
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
@SqlResultSetMapping(
        name = "LocationSearchResultMapping",
        entities = {
                @EntityResult(
                        entityClass = LocationSearchResult.class,
                        fields = {
                                @FieldResult(name = "location_id", column = "location_id"),
                                @FieldResult(name = "country", column = "country_code"),
                                @FieldResult(name = "address", column = "address"),
                                @FieldResult(name = "postalCode", column = "postal_code"),
                                @FieldResult(name = "latitude", column = "latitude"),
                                @FieldResult(name = "longitude", column = "longitude"),
                                @FieldResult(name = "createdBy", column = "created_by"),
                                @FieldResult(name = "createdDate", column = "created_date"),
                                @FieldResult(name = "lastModifiedBy", column = "last_modified_by"),
                                @FieldResult(name = "lastModifiedDate", column = "last_modified_date"),
                                @FieldResult(name = "distance", column = "dist"),
                        }
                )
        }
)
@Data
@Entity
public class LocationSearchResult {

    @Id
    @Column(name = "location_id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String country;

    private String address;

    private String postalCode;

    @Getter(AccessLevel.NONE)
    private double latitude;

    @Getter(AccessLevel.NONE)
    private double longitude;

    private double distance;

    public double getLatitude() {
        return latitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }

    public double getLongitude() {
        return longitude / LocationConstants.CONVERT_COORDINATE_RATIO;
    }
}
