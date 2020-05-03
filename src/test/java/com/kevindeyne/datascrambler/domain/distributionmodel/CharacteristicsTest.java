package com.kevindeyne.datascrambler.domain.distributionmodel;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CharacteristicsTest {

    @Test
    public void ensureUniqueShortcuts() {
        Set<String> shortcuts = new HashSet<>();
        for(Characteristics c : Characteristics.values()) {
            shortcuts.add(c.getShortcutValue());
            Assert.assertEquals(c, Characteristics.findByShortcut(c.getShortcutValue()));
        }
        Assert.assertEquals(Characteristics.values().length, shortcuts.size());
    }

}
