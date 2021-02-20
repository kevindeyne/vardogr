package com.kevindeyne.datascrambler.service;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

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
            if(mac != null) return Base64.getEncoder().encodeToString(mac);
            return Base64.getEncoder().encodeToString(fingerprint().getBytes());
        } catch (SocketException | UnknownHostException e) {
            return System.getProperty("user.name");
        }
    }

    private String fingerprint() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = computerSystem.getSerialNumber();
        String processorIdentifier = centralProcessor.getProcessorIdentifier().getIdentifier();
        int processors = centralProcessor.getLogicalProcessorCount();

        String delimiter = "#";

        return vendor +
                delimiter +
                processorSerialNumber +
                delimiter +
                processorIdentifier +
                delimiter +
                processors;
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
