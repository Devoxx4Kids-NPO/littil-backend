package org.littil;

public class RandomStringGenerator {

    private static final org.apache.commons.text.RandomStringGenerator generator =
            new org.apache.commons.text.RandomStringGenerator.Builder() //
                    .withinRange('a', 'z') //
                    .build();

    public static String generate( int length) {
        return generator.generate(length);
    }
}
