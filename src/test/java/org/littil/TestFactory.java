package org.littil;

import org.apache.commons.lang3.RandomStringUtils;
import org.littil.api.user.service.User;

import java.util.UUID;

public class TestFactory {


    public static User createUser() {
        return createUser(UUID.randomUUID());
    }

    public static User createUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmailAddress(RandomStringUtils.randomAlphabetic(10) + "@littil.org");
        user.setProviderId(UUID.randomUUID().toString());
        return user;
    }
}
