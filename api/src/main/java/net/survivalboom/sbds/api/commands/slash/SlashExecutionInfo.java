package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SlashExecutionInfo extends ExecutionInfo {

    private final SlashCommandInteraction interaction;

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

    public @NotNull User user() {
        return this.interaction.getUser();
    }

    public @NotNull ReplyCallbackAction reply(@NotNull String name, @Nullable Placeholders placeholders) {
        return messages().reply(interaction, placeholders, name, user());
    }

    public @NotNull ReplyCallbackAction reply(@NotNull String name) {
        return messages().reply(interaction, null, name, user());
    }

}
