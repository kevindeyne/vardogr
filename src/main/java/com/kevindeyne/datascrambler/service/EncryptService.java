package com.kevindeyne.datascrambler.service;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.UUID;

@Service
public class EncryptService {

    private static final String ALGORITHM = "PBEWITHSHA1ANDRC4_128";
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;

    public EncryptService() {
        standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setPassword(macAddress());
        standardPBEStringEncryptor.setAlgorithm(ALGORITHM);
    }

    private String macAddress() {
        try {
            final NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            return Base64.getEncoder().encodeToString(mac);
        } catch (SocketException | UnknownHostException e) {
            return System.getProperty("user.name");
        }
    }

    public String encrypt(String clearText){
        if(clearText == null || clearText.equals("")) return "";
        return standardPBEStringEncryptor.encrypt(clearText);
    }

    public String decrypt(String encrypted) {
        if(encrypted == null || encrypted.equals("")) return "";
        return standardPBEStringEncryptor.decrypt(encrypted);
    }

}
