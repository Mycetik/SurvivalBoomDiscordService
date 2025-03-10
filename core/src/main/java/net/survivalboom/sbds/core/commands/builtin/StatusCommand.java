package net.survivalboom.sbds.core.commands.builtin;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.core.messages.Message;
import net.survivalboom.sbds.core.translations.Translation;
import net.survivalboom.sbds.core.translations.TranslationManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Command(name = "status", aliases = "ping", description = "Shows a status of discord bot.")
public class StatusCommand extends CommandBase implements SlashCommand {


    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        TranslationManager translationManager = (TranslationManager) info.sbds().getTranslationManager();
        Translation translation = translationManager.getTranslation0("ukrainian");

        Message message = translation.getMessage(Objects.requireNonNull(info.arguments().get("msg", String.class)));
        if (message == null) {
            info.interaction().reply("Null! " + String.join(" ", translation.getMessages0().stream().map(Message::key).toList())).queue();
            return;
        }

        if (message.text() != null) {
            info.interaction().reply(message.text()).queue();
            return;
        }

        info.interaction().reply(MessageCreateData.fromEmbeds(List.of(message.embeds().getFirst().build(null).build()))).queue();

    }

    @CommandArgument(name = "msg")
    public Argument<?> msg() {
        return new StringArgument();
    }

}
