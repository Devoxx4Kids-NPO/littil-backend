package org.littil.api.location.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.littil.api.auditing.repository.AbstractAuditableEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

/**
 * In order to be able to search in a radius the long and lat coordinates have to be part of the identity.
 * For this reason we use a composite primary key instead of letting the coordinates be a part of the location itself.
 *
 * @See <a href="https://mariadb.com/kb/en/latitudelongitude-indexing/">Latitude/Longitude Indexing</a>
 */

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EntityListeners(LocationEntityListener.class)
@Entity(name = "Location")
@Table(name = "location")
public class LocationEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "location_id", columnDefinition = "BINARY(16)")
    @NonNull
    private UUID id;

    // Code based on ISO-3166
    @Column(name = "country_code", columnDefinition = "VARCHAR(2)")
    @Length(max = 2)
    private String country = "NL";

    @NotEmpty(message = "{Location.address.required}")
    @NonNull
    @Column(name = "address")
    private String address;

    @NotEmpty(message = "{Location.postalCode.required}")
    @NonNull
    @Column(name = "postal_code")
    @Length(max = 10)
    private String postalCode;

    @Column(name = "latitude")
    private int latitude;

    @Column(name = "longitude")
    private int longitude;
}
