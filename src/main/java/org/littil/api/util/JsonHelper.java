package org.littil.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;

@RequestScoped
@Slf4j
public final class JsonHelper {

    private JsonHelper() {
        throw new IllegalStateException("Utility class should not be initialised.");
    }

    public static JsonNode toJson(String message) {
        try {
            return new ObjectMapper().readTree(String.format("{\"message\":\"%s\"}", message));
        } catch (JsonProcessingException e) {
            log.debug("Unable to parse message to json: {}", message, e);
        }
        return null;
    }
}
