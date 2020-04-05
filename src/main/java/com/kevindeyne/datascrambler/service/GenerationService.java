package com.kevindeyne.datascrambler.service;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.PersonProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.Random;

@Service
public class GenerationService {

    private static final Fairy FAIRY = Fairy.create();
    private static final Random RANDOM = new SecureRandom();

    public Object generate(String classIdentifier, int maxLength, String fieldName) {
        if (String.class.getName().equals(classIdentifier)) {
            return generateString(maxLength, fieldName);
        } else if (Integer.class.getName().equals(classIdentifier)) {
            return generateInteger(maxLength);
        } else if(BigDecimal.class.getName().equals(classIdentifier)) {
            return BigDecimal.valueOf(RANDOM.nextDouble());
        } else if(Date.class.getName().equals(classIdentifier)) {
            return new Date(FAIRY.person(PersonProperties.minAge(18)).getDateOfBirth().toEpochDay());
        }
        throw new RuntimeException("Unknown class identifier:" + classIdentifier);
    }

    private Integer generateInteger(int maxLength) {
        return RANDOM.nextInt(maxLength);
    }

    private String generateString(int maxLength, String fieldName) {
        final String fieldNameLower = fieldName.toLowerCase();
        if(fieldNameLower.endsWith("name")) {
            return generateName(maxLength);
        } else if(fieldNameLower.contains("mail")){
            return generateEmail(maxLength);
        } else if(fieldNameLower.contains("address") || fieldNameLower.contains("street")) {
            return generateStreet(maxLength);
        } else if(fieldNameLower.contains("city")){
            return generateCity(maxLength);
        } else if(fieldNameLower.contains("phone") || fieldNameLower.contains("fax")){
            return generatePhone(maxLength);
        } else if(fieldNameLower.contains("postal") || fieldNameLower.contains("zip")){
            return generatePostalCode(maxLength);
        }
        return FAIRY.textProducer().randomString(RANDOM.nextInt(maxLength));
    }

    private String generatePostalCode(int maxLength) {
        String generated = FAIRY.person().getAddress().getPostalCode();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getAddress().getPostalCode();
        }
        return generated;
    }

    private String generatePhone(int maxLength) {
        String generated = FAIRY.person().getTelephoneNumber();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getTelephoneNumber();
        }
        return generated;
    }

    //TODO country/state (and make them make sense)

    private String generateCity(int maxLength) {
        String generated = FAIRY.person().getAddress().getCity();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getAddress().getCity();
        }
        return generated;
    }

    private String generateStreet(int maxLength) {
        String generated = FAIRY.person().getAddress().getStreet();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getAddress().getStreet();
        }
        return generated;
    }

    public String generateEmail(int maxLength) {
        String generated = FAIRY.person().getCompanyEmail();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getCompanyEmail();
        }
        return generated;
    }

    public String generateName(int maxLength) {
        String generated = FAIRY.person().getFirstName();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getFirstName();
        }
        return generated;
    }
}
