package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.misc.EnumSelectArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.LoopMode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command(name = "loop", description = "Set loop mode for current music bot")
public class LoopCommand extends AbstractPlayerCommand {

    public LoopCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) return;

        LoopMode loop = info.arguments().get("mode", LoopMode.class);
        Objects.requireNonNull(loop);

        player.loop(loop);

        User botUser = player.getBot().getBot().getSelfUser();
        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl())
                .add("{MODE}", loop);

        info.reply("music.command.loop").withPlaceholders(placeholders).queue();

    }


    @CommandArgument(name = "mode", description = "Loop mode")
    public Argument<?> mode() {
        return new EnumSelectArgument<>(LoopMode.class);
    }


}
