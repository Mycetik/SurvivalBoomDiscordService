package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Command(name = "pause", description = "Pause or resume current track")
public class PauseCommand extends AbstractPlayerCommand {

    public PauseCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
        sbds.getButtonInteractionManager().registerListener(module, "pause", this::onButtonClick);
    }

    public void onButtonClick(@NotNull ButtonInteractionInfo info) {
        executes0(info, true);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info, false);
    }


    public void executes0(@NotNull IInteractionInfo info, boolean ephemeral) {

        GuildPlayer player = getPlayer(info, false, ephemeral);
        if (player == null) {
            return;
        }

        if (checkBannedOrLocked(info, player, ephemeral)) return;

        boolean state = !player.isPaused();
        player.setPaused(state);

        User botUser = player.getBot().getBot().getSelfUser();
        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl());

        String str = state ? "music.command.pause.paused" : "music.command.pause.resumed";
        info.reply(str)
                .withPlaceholders(placeholders)
                .send()
                .setEphemeral(ephemeral)
                .queue();

    }

    @Override
    public void executes(@NotNull ConsoleExecutionInfo info) {

        AudioChannelUnion channel = info.arguments().get("channel", AudioChannelUnion.class);
        if (channel == null) {
            info.logger().warn("Voice channel not provided.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().warn("No active player found for the given channel.");
            return;
        }

        boolean newState = !player.isPaused();
        player.setPaused(newState);

        User botUser = player.getBot().getBot().getSelfUser();
        String stateStr = newState ? "paused" : "resumed";

        info.logger().info("Track has been {} by {}#{}", stateStr, botUser.getName(), botUser.getDiscriminator());

    }

    @CommandArgument(name = "channel", description = "The voice channel", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
