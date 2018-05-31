package com.kevindeyne.datascrambler.helper;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.util.Random;

public class PrintCmds {

    public static String green(String cmd) {
        return "\033[0;32m" + cmd + "\033[0m";
    }

    public static void printProgress() {
        try (ProgressBar pb = new ProgressBar("Scrambling data ...", 100, ProgressBarStyle.COLORFUL_UNICODE_BLOCK )) {
            for (int i = 0; i < 100; i++) {
                try {
                    pb.step();
                    Thread.sleep(new Random().nextInt(600));
                } catch (Exception e) {
                }
            }
        }
    }

}
