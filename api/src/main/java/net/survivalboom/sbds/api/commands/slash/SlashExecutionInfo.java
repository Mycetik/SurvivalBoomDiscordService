package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.HookEditable;
import net.survivalboom.sbds.api.interaction.MessageReplyable;
import net.survivalboom.sbds.api.interaction.ModalReplyable;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public class SlashExecutionInfo extends CommandExecutionInfo implements ModalReplyable, MessageReplyable, HookEditable {

    protected final SlashCommandInteraction interaction;

    public SlashExecutionInfo(@NotNull Command command, @NotNull SlashCommandInteraction interaction, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.interaction = interaction;
    }

    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    public @Nullable Guild guild() {
        return this.interaction.getGuild();
    }

    public @Nullable Member guildMember() {
        return this.interaction.getMember();
    }

    @Override
    public @NotNull InteractionHook hook() {
        return interaction.getHook();
    }

    @Override
    public @NotNull IReplyCallback replyCallback() {
        return interaction;
    }

    @Override
    public @NotNull IModalCallback modalCallback() {
        return interaction;
    }

    public @NotNull User user() {
        return this.interaction.getUser();
    }


    protected @NotNull IMessage getMessage(@NotNull String key) {
        return Objects.requireNonNull(messages().getMessage(key, user(), true));
    }

}
