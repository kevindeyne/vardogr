package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.Characteristics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;

class CharacteristicServiceTest {

    private final CharacteristicService service = new CharacteristicService();

    @Test
    void testPositiveNumbers() {
        List<String> shortcuts = service.determineCharacteristics(5L, Long.MAX_VALUE);
        Assertions.assertEquals(1, shortcuts.size());
        Assertions.assertEquals(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    void testNegativeNumbers() {
        List<String> shortcuts = service.determineCharacteristics(Long.MIN_VALUE, -1L);
        Assertions.assertEquals(1, shortcuts.size());
        Assertions.assertEquals(Characteristics.CAN_BE_NEGATIVE_NUMBER.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    void testPastOnly() {
        List<String> shortcuts = service.determineCharacteristics(new Date(System.currentTimeMillis() - 10000L), new Date(System.currentTimeMillis()));
        Assertions.assertEquals(1, shortcuts.size());
        Assertions.assertEquals(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    void testPastAndFutureDates() {
        List<String> shortcuts = service.determineCharacteristics(new Date(System.currentTimeMillis() - 10000L), new Date(System.currentTimeMillis() + 10000L));
        Assertions.assertEquals(2, shortcuts.size());
        Assertions.assertTrue(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue().equals(shortcuts.get(0)) || Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue().equals(shortcuts.get(0)));
        Assertions.assertTrue(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue().equals(shortcuts.get(1)) || Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue().equals(shortcuts.get(1)));
    }

}
