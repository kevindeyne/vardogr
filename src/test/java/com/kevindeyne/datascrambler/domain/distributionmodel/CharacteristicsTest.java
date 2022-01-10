package com.kevindeyne.datascrambler.domain.distributionmodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class CharacteristicsTest {

    @Test
    void ensureUniqueShortcuts() {
        Set<String> shortcuts = new HashSet<>();
        for(Characteristics c : Characteristics.values()) {
            shortcuts.add(c.getShortcutValue());
            Assertions.assertEquals(c, Characteristics.findByShortcut(c.getShortcutValue()));
        }
        Assertions.assertEquals(Characteristics.values().length, shortcuts.size());
    }

}
