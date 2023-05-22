package org.littil;

import org.apache.commons.lang3.RandomStringUtils;
import org.littil.api.user.service.User;

import java.util.UUID;

public class TestFactory {


    public static User createUser(UUID id, String emailAddress) {
        User user = new User();
        user.setId(id);
        user.setEmailAddress(emailAddress);
        return user;
    }

    public static User createUser(UUID id) {
        return createUser(id,RandomStringUtils.randomAlphabetic(10) + "@littil.org");
    }

    public static User createUser(String email) {
        return createUser(null,email);
    }

    public static User createUser() {
        return createUser((UUID)null);
    }
}
