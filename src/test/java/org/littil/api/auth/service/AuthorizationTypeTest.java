package org.littil.api.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationTypeTest {

    @ParameterizedTest
    @EnumSource(AuthorizationType.class)
    void authorizationIds(AuthorizationType type) {
        var result = type.authorizationIds(Collections.emptyMap());

        assertNotNull(result);
        assertEquals(0,result.count());
    }

    @Test
    void authorizationIdsOnSchool() {
        String id = "9beae92a-c735-454d-9535-20a54b411b5c";
        Map<String,List<String>> authorizations = Map.of("schools", List.of(id));

        assertEquals(Optional.of(UUID.fromString(id)),AuthorizationType.SCHOOL.authorizationIds(authorizations).findFirst());
    }

    @Test
    void authorizationIdsOnGuestTeacher() {
        String id = "9beae92a-c735-454d-9535-20a54b411b5c";
        Map<String,List<String>> authorizations = Map.of("guest_teachers", List.of(id));

        assertEquals(Optional.of(UUID.fromString(id)),AuthorizationType.GUEST_TEACHER.authorizationIds(authorizations).findFirst());
    }
}