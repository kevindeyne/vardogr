package com.kevindeyne.datascrambler.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class JFairyTest {

    GenerationHelperService generationHelperService = new GenerationHelperService();

    @Test
    void testUniquenessAddress() {
        Set<String> streets = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            streets.add(generationHelperService.generateStreet(255));
        }
        System.out.println("Unique streets: " + streets.size());
        Assertions.assertTrue(streets.size() > 9500);
    }

    @Test
    void testUniquenessRandom() {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            names.add(generationHelperService.randomName());
        }
        System.out.println("Unique names: " + names.size());
        Assertions.assertTrue(names.size() > 9500);
    }

}
