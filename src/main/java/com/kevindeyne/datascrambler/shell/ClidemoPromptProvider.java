package com.kevindeyne.datascrambler.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ClidemoPromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("BCLONE:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
    }
}
