package org.littil.api.location.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LocationRepository implements PanacheRepositoryBase<LocationEntity, UUID> {

    @Inject
    Session session;

    public void findLocationsOrderedByDistance(Double latitude, Double longtitude, int  startDistance, int maxDistance, int limit, String condition) {
        String sql = "CALL FindNearest(:latitude, :Longtitude, :startDistance, :maxDistance, :limit, :condition);";

        Query query = session.createSQLQuery(sql)
                .setParameter("latitude", latitude)
                .setParameter("Longtitude", longtitude)
                .setParameter("startDistance", startDistance)
                .setParameter("maxDistance", maxDistance)
                .setParameter("limit", limit)
                .setParameter("condition", condition);

        List result = query.getResultList();
    }
}
