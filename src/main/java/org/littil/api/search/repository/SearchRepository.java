package org.littil.api.search.repository;

import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class SearchRepository {

    @Inject
    Session session;

    public List<LocationSearchResult> findLocationsOrderedByDistance(double latitude, double longitude, int  startDistance, int maxDistance, int limit, String condition) {
        String sql = "CALL FindNearest(:latitude, :longitude, :startDistance, :maxDistance, :limit, :condition);";

        List<LocationSearchResult> result = session.createNativeQuery(sql, "LocationSearchResultMapping")
                .setParameter("latitude", latitude)
                .setParameter("longitude", longitude)
                .setParameter("startDistance", startDistance)
                .setParameter("maxDistance", maxDistance)
                .setParameter("limit", limit)
                .setParameter("condition", condition)
                .getResultList();

        return result.stream()
                .distinct()
                .toList();
    }
}