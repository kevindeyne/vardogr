package com.kevindeyne.datascrambler.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class GenerationServiceTest {

    private GenerationService generationService;

    @Before
    public void init() {
        generationService = new GenerationService();
    }

    @Test
    public void testGenerateString() {
        int maxLength = new Random().nextInt(53);
        String stringData = (String) generationService.generate(String.class.getName(), maxLength, "firstName");
        Assert.assertNotNull(stringData);
        Assert.assertTrue(stringData.length() <= maxLength);
        System.out.println(stringData);
    }


    //java.lang.Integer
    //java.sql.Date

}
