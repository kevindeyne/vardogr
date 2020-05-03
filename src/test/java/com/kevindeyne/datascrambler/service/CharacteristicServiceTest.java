package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.distributionmodel.Characteristics;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

public class CharacteristicServiceTest {

    private CharacteristicService service = new CharacteristicService();

    @Test
    public void testPositiveNumbers() {
        List<String> shortcuts = service.determineCharacteristics(5L, Long.MAX_VALUE);
        Assert.assertEquals(1, shortcuts.size());
        Assert.assertEquals(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    public void testNegativeNumbers() {
        List<String> shortcuts = service.determineCharacteristics(Long.MIN_VALUE, -1L);
        Assert.assertEquals(1, shortcuts.size());
        Assert.assertEquals(Characteristics.CAN_BE_NEGATIVE_NUMBER.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    public void testPastOnly() {
        List<String> shortcuts = service.determineCharacteristics(new Date(System.currentTimeMillis()  - 10000L), new Date(System.currentTimeMillis()));
        Assert.assertEquals(1, shortcuts.size());
        Assert.assertEquals(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue(), shortcuts.get(0));
    }

    @Test
    public void testPastAndFutureDates() {
        List<String> shortcuts = service.determineCharacteristics(new Date(System.currentTimeMillis()  - 10000L), new Date(System.currentTimeMillis()  + 10000L));
        Assert.assertEquals(2, shortcuts.size());
        Assert.assertTrue(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue().equals(shortcuts.get(0)) || Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue().equals(shortcuts.get(0)));
        Assert.assertTrue(Characteristics.CAN_BE_DATE_IN_PAST.getShortcutValue().equals(shortcuts.get(1)) || Characteristics.CAN_BE_DATE_IN_FUTURE.getShortcutValue().equals(shortcuts.get(1)));
    }

}
