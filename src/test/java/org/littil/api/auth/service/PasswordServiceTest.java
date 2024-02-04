package org.littil.api.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    @Test
    void generate() {
        var sut = new PasswordService();
        sut.init();

        var generated = sut.generate();
        assertEquals(13,generated.length());
        assertEquals(6,generated.indexOf("-"));
    }
}