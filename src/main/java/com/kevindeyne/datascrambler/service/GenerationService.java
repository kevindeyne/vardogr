package com.kevindeyne.datascrambler.service;

import com.devskiller.jfairy.Fairy;
import com.kevindeyne.datascrambler.domain.distributionmodel.Characteristics;
import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream resource = classloader.getResourceAsStream("names.all.txt")) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> loadCountryMap() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream resource = classloader.getResourceAsStream("country.txt")) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).lines()
                    .map(s -> s.split(",")).collect(Collectors.toMap(a -> a[0].trim(), a -> a.length>1? a[1].trim(): ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object generate(String classIdentifier, int maxLength, FieldData field) {
        if (String.class.getName().equals(classIdentifier)) {
            return generateString(maxLength, field.getFieldName());
        } else if (Integer.class.getName().equals(classIdentifier)) {
            if(field.isPrimaryKey()) {
                field.setOffset(field.getOffset() + 1);
                return field.getOffset();
            } else {
                return generateInteger(maxLength, field.getCharacteristics());
            }
        } else if (Short.class.getName().equals(classIdentifier)) {
            return Math.abs(RANDOM.nextInt(Short.MAX_VALUE));
        } else if(BigDecimal.class.getName().equals(classIdentifier)) {
            return BigDecimal.valueOf(RANDOM.nextDouble());
        } else if(Date.class.getName().equals(classIdentifier)) {
            return generateDate(field.getCharacteristics());
        } else if(Timestamp.class.getName().equals(classIdentifier)) {
            return generateTimestamp(field.getCharacteristics());
        } else if(Boolean.class.getName().equals(classIdentifier)) {
            return RANDOM.nextInt(1) == 0;
        }
        throw new RuntimeException("Unknown class identifier:" + classIdentifier);
    }

    private Timestamp generateTimestamp(List<String> characteristics) {
        long randomDay = generateDateLong(characteristics);
        return new Timestamp(randomDay);
    }

    private LocalDate generateDate(List<String> characteristics) {
        long randomDay = generateDateLong(characteristics);
        return LocalDate.ofEpochDay(randomDay);
    }

    private long generateDateLong(List<String> characteristics) {
        boolean canHaveFutureValues = false;
        boolean canHavePastValues = false;
        if (null != characteristics) {
            for (String shortcut : characteristics) {
                if (Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue().equals(shortcut))
                    canHaveFutureValues = true;
                if (Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue().equals(shortcut)) canHavePastValues = true;
            }
        }

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int minDay = (int) LocalDate.now().toEpochDay();
        int maxDay = (int) LocalDate.of(year + 30, 1, 1).toEpochDay();

        if (canHavePastValues && canHaveFutureValues) {
            minDay = (int) LocalDate.of(year - 30, 1, 1).toEpochDay();
        } else if (canHavePastValues) {
            minDay = (int) LocalDate.of(year - 30, 1, 1).toEpochDay();
            maxDay = (int) LocalDate.now().toEpochDay();
        }

        return minDay + RANDOM.nextInt(maxDay - minDay);
    }

    private Integer generateInteger(int maxLength, List<String> characteristics) {
        boolean canHavePositiveValue = false;
        boolean canHaveNegativeValue = false;
        if(null != characteristics) {
            for(String shortcut : characteristics) {
                if(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue().equals(shortcut)) canHavePositiveValue = true;
                if(Characteristics.CAN_BE_NEGATIVE_NUMBER.getShortcutValue().equals(shortcut)) canHaveNegativeValue = true;
            }
        }

        int m = (int) Math.pow(10, maxLength - 1);
        int value = RANDOM.nextInt(9 * m);
        if(canHavePositiveValue && canHaveNegativeValue) {
            return value;
        } else if(canHavePositiveValue) {
            return Math.abs(value);
        } else {
            return -Math.abs(value);
        }
    }

    public String generateString(int maxLength, String fieldName) {
        if(maxLength > 1000) maxLength = 1000;
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
        return String.valueOf(generateInteger(maxLength, Collections.singletonList(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue())));
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
