package com.kevindeyne.datascrambler.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionTest {

    private EncryptService service;

    @BeforeEach
    public void init() {
        service = new EncryptService();
    }

    @Test
    void testEncryption() {
        String privateData = "secret-data";

        String myEncryptedText = service.encrypt(privateData);
        System.out.println(myEncryptedText);
        Assertions.assertNotSame(privateData, myEncryptedText);

        String plainText = service.decrypt(myEncryptedText);
        Assertions.assertEquals(plainText, privateData);
    }
}
