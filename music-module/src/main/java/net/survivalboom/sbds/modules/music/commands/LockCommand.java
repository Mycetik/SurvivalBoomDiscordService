package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.discord.channel.VoiceChannelArgument;
import net.survivalboom.sbds.api.commands.base.CommandClass;
import net.survivalboom.sbds.api.commands.base.ArgumentMethod;
import net.survivalboom.sbds.api.commands.console.ConsoleExecutionInfo;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.interaction.button.ButtonInteractionInfo;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@CommandClass(name = "music-lock", description = "Locks current music bot for staff usage only", translationKey = "music.command.lock", permission = "music.command.lock")
public class LockCommand extends AbstractPlayerCommand {

    public LockCommand(@NotNull BotManager botManager) {
        super(botManager);
    }

    @Override
    protected void init(@NotNull ISBDS sbds, @Nullable IModule module) {
        Objects.requireNonNull(module);
        sbds.getButtonInteractionManager().registerListener(module, "lock", this::onButtonClick, getPermission());
    }

    public void onButtonClick(@NotNull ButtonInteractionInfo info) {
        executes0(info, true);
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {
        executes0(info, false);
    }


    private void executes0(@NotNull IInteractionInfo info, boolean ephemeral) {

        GuildPlayer player = getPlayer(info, false, ephemeral);
        if (player == null) {
            return;
        }

        boolean state = !player.adminLock();

        player.adminLock(state);

        User botUser = player.getBot().getBot().getSelfUser();
        Placeholders placeholders = new Placeholders()
                .add("{BOT}", botUser.getEffectiveName() + "#" + botUser.getDiscriminator())
                .add("{BOT-AVATAR}", botUser.getEffectiveAvatarUrl());


        String str = state ? "music.command.lock.locked" : "music.command.lock.unlocked";
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
            info.logger().error("Channel argument is missing or invalid.");
            return;
        }

        GuildPlayer player = getPlayer(info, channel, false);
        if (player == null) {
            info.logger().error("No player found for the provided channel.");
            return;
        }

        boolean newState = !player.adminLock(); // toggle lock
        player.adminLock(newState);

        String msg = newState ? "Music bot is now &blocked &rfor staff-only use." : "Music bot is now &bunlocked &rfor all users.";
        info.logger().info(msg);

    }

    @ArgumentMethod(name = "channel", description = "Channel with bot", scope = ArgumentScope.CONSOLE)
    public Argument<?> channel() {
        return new VoiceChannelArgument();
    }

}
