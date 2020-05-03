package com.kevindeyne.datascrambler.domain.distributionmodel;

import lombok.Getter;

public enum Characteristics {

    CAN_BE_BLANK("b"),
    CAN_HAVE_SPACE("s"),
    CAN_BE_DATE_IN_FUTURE("f"),
    CAN_BE_DATE_IN_PAST("p"),
    CAN_BE_NEGATIVE_NUMBER("-"),
    CAN_BE_POSITIVE_NUMBER("+");

    @Getter
    private final String shortcutValue;

    Characteristics(String shortcutValue) {
        this.shortcutValue = shortcutValue;
    }

    public static Characteristics findByShortcut(String shortcutValue) {
        for(Characteristics c : Characteristics.values()) {
            if(c.getShortcutValue().equals(shortcutValue)) return c;
        }
        return null;
    }

}
