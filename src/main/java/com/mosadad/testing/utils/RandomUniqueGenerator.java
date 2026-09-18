package com.mosadad.testing.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomUniqueGenerator {

    private static final Set<Integer> issuedNumbers = new HashSet<>();
    private static final Random random = new Random();

    public static synchronized int generateRandom7Digit() {
        if (issuedNumbers.size() >= 9000000) {
            throw new IllegalStateException("Pool almost full, collisions too high!");
        }

        int number;
        do {
            // Generates between 1000000 and 9999999
            number = 1000000 + random.nextInt(9000000);
        } while (issuedNumbers.contains(number)); // Retry if already used

        issuedNumbers.add(number);
        return number;
    }
}
