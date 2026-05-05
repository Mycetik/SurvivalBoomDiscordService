package net.survivalboom.sbds.modules.test.commands.context;

import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ContextCommandBase;
import net.survivalboom.sbds.api.commands.context.UserContextCommand;
import net.survivalboom.sbds.api.commands.context.UserContextInteractionInfo;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

@CommandClass(name = "test-user")
public class TestUserContext extends ContextCommandBase implements UserContextCommand {

    @Override
    public void execute(@NotNull UserContextInteractionInfo info) {
        String user = info.event().getTargetMember().getEffectiveName();
        info.reply("test.context.user").withPlaceholders(Placeholders.of("{USER}", user)).queue();
    }

}
