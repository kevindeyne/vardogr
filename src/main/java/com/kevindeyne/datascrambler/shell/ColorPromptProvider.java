package com.kevindeyne.datascrambler.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ColorPromptProvider implements PromptProvider {

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("VARDØGR:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
    }
}
