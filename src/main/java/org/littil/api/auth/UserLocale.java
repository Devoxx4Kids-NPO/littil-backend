package org.littil.api.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * UserLocale only support The Netherlands in the pilot phase for now.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserLocale {
    private static final Map<String, Locale> localeMapping = new HashMap<>();

    static {
        localeMapping.put("NL", new Locale("nl", "NL"));
    }

    public static Optional<Locale> getLocaleFromCountyCode(String countryCode) {
        return Optional.of(localeMapping.get(countryCode));
    }
}
