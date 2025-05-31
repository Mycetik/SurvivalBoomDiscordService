package net.survivalboom.sbds.modules.test.commands.context;

import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.ContextCommandBase;
import net.survivalboom.sbds.api.commands.context.MessageContextCommand;
import net.survivalboom.sbds.api.commands.context.MessageContextInteractionInfo;
import org.jetbrains.annotations.NotNull;

@Command(name = "test")
public class TestMessageContext extends ContextCommandBase implements MessageContextCommand {

    @Override
    public void execute(@NotNull MessageContextInteractionInfo info) {
        info.event().reply(info.event().getTarget().getContentRaw()).queue();
    }

}
