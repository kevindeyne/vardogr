package com.kevindeyne.datascrambler.service;

import org.junit.Assert;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

public class JFairyTest {

    GenerationService generationService = new GenerationService();

    @Test
    public void testUniquenessAddress() {
        Set<String> streets = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            streets.add(generationService.generateStreet(255));
        }
        System.out.println("Unique streets: " + streets.size());
        Assert.assertTrue(streets.size() > 9500);
    }

    @Test
    public void testUniquenessRandom() {
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            names.add(generationService.randomName());
        }
        System.out.println("Unique names: " + names.size());
        Assert.assertTrue(names.size() > 9500);
    }

}
