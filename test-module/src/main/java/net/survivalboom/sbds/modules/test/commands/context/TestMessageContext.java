package net.survivalboom.sbds.modules.test.commands.context;

import net.dv8tion.jda.api.entities.Message;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.ContextCommandBase;
import net.survivalboom.sbds.api.commands.context.MessageContextCommand;
import net.survivalboom.sbds.api.commands.context.MessageContextInteractionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;

@Command(name = "test-message")
public class TestMessageContext extends ContextCommandBase implements MessageContextCommand {

    @Override
    public void execute(@NotNull MessageContextInteractionInfo info) {

        Message message = info.event().getTarget();
        String id = message.getId();
        String author = message.getAuthor().getEffectiveName();
        String text = message.getContentRaw();

        info.reply("test.context.message")
                .withPlaceholders(Placeholders.of("{AUTHOR}", author, "{ID}", id, "{TEXT}", text))
                .queue();

    }

}
