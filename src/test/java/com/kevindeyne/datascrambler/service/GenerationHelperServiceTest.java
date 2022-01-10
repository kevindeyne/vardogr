package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

class GenerationHelperServiceTest {

    private GenerationHelperService generationHelperService;

    @BeforeEach
    void init() {
        generationHelperService = new GenerationHelperService();
    }

    @Test
    void testGenerateString() {
        int maxLength = new Random().nextInt(53);
        FieldData field = new FieldData("firstName");
        String stringData = (String) generationHelperService.generate(String.class.getName(), maxLength, field);
        Assertions.assertNotNull(stringData);
        Assertions.assertTrue(stringData.length() <= maxLength);
        System.out.println(stringData);
    }

    @Test
    void test() {
        String string = generationHelperService.randomName();
        System.out.println(string);
    }

}
