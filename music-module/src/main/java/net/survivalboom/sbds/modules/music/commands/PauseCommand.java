package net.survivalboom.sbds.modules.music.commands;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.Command;
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

}
