package org.littil;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.UUID;

public class Helper {
    public static String getErrorMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }

    public static JsonObject withGuestTeacherAuthorization() {
        return withGuestTeacherAuthorization(UUID.randomUUID());
    }

    public static JsonObject withGuestTeacherAuthorization(final UUID id) {
        return withAuthorization("guest_teachers", id);
    }

    public static JsonObject withSchoolAuthorization() {
        return withSchoolAuthorization(UUID.randomUUID());
    }

    public static JsonObject withSchoolAuthorization(final UUID id) {
        return withAuthorization("schools", id);
    }

    private static JsonObject withAuthorization(final String name, final UUID id) {
        JsonBuilderFactory factory = Json.createBuilderFactory(Collections.emptyMap());
        return factory.createObjectBuilder().add(name, factory.createArrayBuilder().add(id.toString())).build();
    }


}
