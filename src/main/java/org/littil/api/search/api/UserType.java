package org.littil.api.search.api;

import java.util.Arrays;
import java.util.Optional;

public enum UserType {
    SCHOOL("school"),
    GUEST_TEACHER("guest_teacher");

    public final String label;

    public String getLabel() {
        return this.label;
    }

    private UserType(String label) {
        this.label = label;
    }

    public static Optional<UserType> findByLabel(String label) {
        return Arrays.stream(UserType.values())
                .filter(userType -> userType.getLabel().equalsIgnoreCase(label))
                .findFirst();
    }
}