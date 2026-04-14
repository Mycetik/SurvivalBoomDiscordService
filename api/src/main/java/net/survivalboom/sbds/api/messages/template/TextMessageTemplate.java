package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.typemap.ModifiableTypeMap;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TextMessageTemplate implements IMessageTemplate {

    private final String text;

    public TextMessageTemplate(@NotNull String text) {
        this.text = text;
    }

    public @NotNull String getText() {
        return text;
    }

    @Override
    public @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return MessageCreateData.fromContent(Objects.requireNonNull(StringParser.stParseNullable(parser, text)));
    }

    @Override
    public void dump(@NotNull ModifiableTypeMap map) {
        map.put("$content", text);
    }

}
