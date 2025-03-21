package org.littil;

import org.littil.api.user.service.User;

import java.util.UUID;

public class TestFactory {


    public static User createUser() {
        return createUser(UUID.randomUUID());
    }

    public static User createUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmailAddress(RandomStringGenerator.generate(10) + "@littil.org");
        return user;
    }
}
