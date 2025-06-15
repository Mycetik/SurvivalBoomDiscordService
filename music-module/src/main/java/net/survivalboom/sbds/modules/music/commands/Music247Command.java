package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;

@Command(name = "music-24-7", description = "Disable idle disconnect for the current music bot", permission = "music.command.24_7")
public class Music247Command extends AbstractPlayerCommand {

    public Music247Command(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) throws Throwable {

        GuildPlayer player = getPlayer(info, false, false);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, false)) {
            return;
        }

        boolean state = !player.idleDisconnect();
        player.idleDisconnect(state);

        String str = state ? "music.command.24-7.disable" : "music.command.24-7.enable";

        User botUser = player.getBot().getBot().getSelfUser();
        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl());

        info.reply(str).withPlaceholders(placeholders).queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        if (channel == null) {
            info.logger().error("Missing or invalid 'channel' argument.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No music player found for the specified channel.");
            return;
        }

        boolean newState = !player.idleDisconnect();
        player.idleDisconnect(newState);

        info.logger().info("24/7 mode is now {}", newState ? "ENABLED (idle disconnect disabled)" : "DISABLED (idle disconnect enabled)");

    }

    @CommandArgument(name = "channel", description = "Voice channel where the bot is playing", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
