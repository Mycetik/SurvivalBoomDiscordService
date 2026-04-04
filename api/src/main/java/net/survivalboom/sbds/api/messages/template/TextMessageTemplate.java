package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

public class TextMessageTemplate implements IMessageTemplate {

    private final String text;

    public TextMessageTemplate(@NotNull String text) {
        this.text = text;
    }

    public @NotNull String getText() {
        return text;
    }

    @Override
    public @NotNull MessageCreateData createMessageData() {
        return MessageCreateData.fromContent(text);
    }

}
