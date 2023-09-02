package org.littil.api.search.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class SearchRepository {

    @Inject
    EntityManager entityManager;

    public List<LocationSearchResult> findLocationsOrderedByDistance(double latitude, double longitude, int startDistance, int maxDistance, int limit, String condition) {
        @SuppressWarnings("unchecked")
        List<LocationSearchResult> result = entityManager.createNamedStoredProcedureQuery("FindWithinRadius")
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