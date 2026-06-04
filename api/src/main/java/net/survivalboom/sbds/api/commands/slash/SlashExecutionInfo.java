package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.InteractionHolder;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public class SlashExecutionInfo extends CommandExecutionInfo<ISlashCommandManager.IRegisteredSlashCommand, ISlashCommandManager> implements InteractionHolder {

    protected final SlashCommandInteraction interaction;

    public SlashExecutionInfo(
            @NotNull SlashCommandInteraction interaction,
            @NotNull ISlashCommandManager.IRegisteredSlashCommand rootCommand,
            @NotNull Command currentCommand,
            @NotNull String alias,
            @NotNull TypeMap arguments
    ) {
        super(rootCommand, currentCommand, alias, arguments);
        this.interaction = interaction;
    }

    @Override
    public @NotNull Object source() {
        return interaction;
    }

    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    @Override
    public Guild guild() {
        return interaction.getGuild();
    }

    @Override
    public @NotNull User user() {
        return interaction.getUser();
    }

    @Override
    public Member member() {
        return interaction.getMember();
    }

    @Override
    public Channel channel() {
        return interaction.getChannel();
    }

    // EDIT //

    @Override
    public @NotNull RestAction<?> editRaw(@NotNull String txt) {
        return interaction.getHook().editOriginal(txt);
    }

    @Override
    public @NotNull RestAction<?> edit(@NotNull MessageEditData data) {
        return interaction.getHook().editOriginal(data);
    }

    // REPLY //

    @Override
    public @NotNull RestAction<?> replyRaw(@NotNull String txt, boolean ephemeral) {
        return interaction.reply(txt).setEphemeral(ephemeral);
    }

    @Override
    public @NotNull RestAction<?> reply(@NotNull MessageCreateData data, boolean ephemeral) {
        return interaction.reply(data).setEphemeral(ephemeral);
    }

}
