package org.littil.api.location.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.littil.api.auditing.repository.AbstractAuditableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

/**
 * In order to be able to search in a radius the long and lat coordinates have to be part of the identity.
 * For this reason we use a composite primary key instead of letting the coordinates be a part of the location itself.
 *
 * @See <a href="https://mariadb.com/kb/en/latitudelongitude-indexing/">Latitude/Longitude Indexing</a>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Location")
@Table(name = "location")
public class LocationEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "location_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NotEmpty(message = "{Location.address.required}")
    @Column(name = "address")
    private String address;

    @NotEmpty(message = "{Location.postalCode.required}")
    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "latitude")
    private Integer latitude;

    @Column(name = "longitude")
    private Integer longitude;
}
