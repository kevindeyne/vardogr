package com.kevindeyne.datascrambler;

import com.kevindeyne.datascrambler.service.EncryptService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class EncryptionTest {

    private EncryptService service;

    @Before
    public void init() {
        service = new EncryptService();
    }

    @Test
    public void testEncryption() {
        String privateData = "secret-data";

        String myEncryptedText = service.encrypt(privateData);
        System.out.println(myEncryptedText);
        assertNotSame(privateData, myEncryptedText);

        String plainText = service.decrypt(myEncryptedText);
        assertEquals(plainText, privateData);
    }
}
