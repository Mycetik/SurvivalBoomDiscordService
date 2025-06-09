package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.IInteractionInfo;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SlashExecutionInfo extends CommandExecutionInfo implements IInteractionInfo {

    protected final SlashCommandInteraction interaction;

    public SlashExecutionInfo(@NotNull Command command, @NotNull SlashCommandInteraction interaction, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.interaction = interaction;
    }

    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    @Override
    public @Nullable Guild guild() {
        return this.interaction.getGuild();
    }

    @Override
    public Member member() {
        return interaction.getMember();
    }

    @Override
    public @NotNull IReplyCallback replyCallback() {
        return interaction;
    }

    @Override
    public @NotNull IModalCallback modalCallback() {
        return interaction;
    }

    @Override
    public @NotNull InteractionHook hook() {
        return interaction.getHook();
    }

    public @NotNull User user() {
        return this.interaction.getUser();
    }

}
