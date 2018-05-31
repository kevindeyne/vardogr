package com.kevindeyne.datascrambler.helper;

public class PrintCmds {

    public static String green(String cmd) {
        return "\033[0;32m" + cmd + "\033[0m";
    }

}
