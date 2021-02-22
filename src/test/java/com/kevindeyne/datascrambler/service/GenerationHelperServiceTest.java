package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Random;

public class GenerationHelperServiceTest {

    private GenerationHelperService generationHelperService;

    @Before
    public void init() {
        generationHelperService = new GenerationHelperService();
    }

    @Test
    public void testGenerateString() {
        int maxLength = new Random().nextInt(53);
        FieldData field = new FieldData("firstName");
        String stringData = (String) generationHelperService.generate(String.class.getName(), maxLength, field);
        Assert.assertNotNull(stringData);
        Assert.assertTrue(stringData.length() <= maxLength);
        System.out.println(stringData);
    }

    @Test
    public void test() throws FileNotFoundException {
        String string = generationHelperService.randomName();
        System.out.println(string);
    }
    //java.lang.Integer
    //java.sql.Date

}
