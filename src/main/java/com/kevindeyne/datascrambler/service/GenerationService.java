package com.kevindeyne.datascrambler.service;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.PersonProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GenerationService {

    private static final Fairy FAIRY = Fairy.create();
    private static final Random RANDOM = new SecureRandom();
    private static final List<String> NAME_LIST = loadNameList();
    private static final Map<String, String> COUNTRY_MAP = loadCountryMap();
    private static final List<String> STREET_LIST = loadStreetList();

    private static List<String> loadStreetList() {
        return Arrays.asList("St", "Pl", "Rd", "Ln", "Blvd", "Ave", "Dr");
    }

    private static List<String> loadNameList() {
        File file = new File(GenerationService.class.getClassLoader().getResource("names.all.txt").getFile());
        try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
            return stream.distinct().collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> loadCountryMap() {
        File file = new File(GenerationService.class.getClassLoader().getResource("country.txt").getFile());
        try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
            return stream.map(s -> s.split(",")).collect(Collectors.toMap(a -> a[0].trim(), a -> a.length>1? a[1].trim(): ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object generate(String classIdentifier, int maxLength, String fieldName) {
        if (String.class.getName().equals(classIdentifier)) {
            return generateString(maxLength, fieldName);
        } else if (Integer.class.getName().equals(classIdentifier)) {
            return generateInteger(maxLength);
        } else if(BigDecimal.class.getName().equals(classIdentifier)) {
            return BigDecimal.valueOf(RANDOM.nextDouble());
        } else if(Date.class.getName().equals(classIdentifier)) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int minDay = (int) LocalDate.of(year-130, 1, 1).toEpochDay();
            int maxDay = (int) LocalDate.of(year-16, 1, 1).toEpochDay();
            long randomDay = minDay + RANDOM.nextInt(maxDay - minDay);
            return LocalDate.ofEpochDay(randomDay);
        }
        throw new RuntimeException("Unknown class identifier:" + classIdentifier);
    }

    private Integer generateInteger(int maxLength) {
        int m = (int) Math.pow(10, maxLength - 1);
        return Math.abs(RANDOM.nextInt(9 * m));
    }

    public String generateString(int maxLength, String fieldName) {
        final String fieldNameLower = fieldName.toLowerCase();
        if(fieldNameLower.endsWith("name")) {
            return generateName(maxLength);
        } else if(fieldNameLower.contains("mail")){
            return generateEmail(maxLength);
        } else if(fieldNameLower.contains("address") || fieldNameLower.contains("street")) {
            return generateStreet(maxLength);
        } else if(fieldNameLower.contains("city")){
            return generateCity(maxLength);
        } else if(fieldNameLower.contains("country")){
            return generateCountry(maxLength);
        } else if(fieldNameLower.contains("phone") || fieldNameLower.contains("fax")){
            return generatePhone(maxLength);
        } else if(fieldNameLower.contains("postal") || fieldNameLower.contains("zip")){
            return generateNumberString(maxLength);
        }
        return FAIRY.textProducer().randomString(RANDOM.nextInt(maxLength));
    }

    private String generateCountry(int maxLength) {
        String countryKey = new ArrayList<>(COUNTRY_MAP.keySet()).get(RANDOM.nextInt(COUNTRY_MAP.keySet().size()));
        if(maxLength == 2) {
            return COUNTRY_MAP.get(countryKey);
        } else {
            return countryKey;
        }
    }

    private String generateNumberString(int maxLength) {
        return String.valueOf(generateInteger(maxLength));
    }

    private String generatePhone(int maxLength) {
        String generated = FAIRY.person().getTelephoneNumber();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getTelephoneNumber();
        }
        return generated;
    }

    //TODO state (and make them make sense)

    public String generateCity(int maxLength) {
        String generated = FAIRY.person().getAddress().getCity();
        while (generated.length() > maxLength) {
            generated = FAIRY.person().getAddress().getCity();
        }
        return generated;
    }

    public String generateStreet(int maxLength) {
        String suffix = " " + STREET_LIST.get(RANDOM.nextInt(STREET_LIST.size()));
        String generated = randomName() + suffix;
        while (generated.length() > maxLength - suffix.length()) {
            generated = randomName() + suffix;
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
        String generated = randomName();
        while (generated.length() > maxLength) {
            generated = randomName();
        }
        return generated;
    }

    public String randomName() {
        return NAME_LIST.get(RANDOM.nextInt(NAME_LIST.size()));
    }
}
