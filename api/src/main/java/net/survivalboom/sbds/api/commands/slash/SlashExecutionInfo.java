package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ExecutionInfo;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SlashExecutionInfo extends ExecutionInfo {

    private final SlashCommandInteraction interaction;

    private final User user;

    public SlashExecutionInfo(@NotNull Command command, @NotNull SlashCommandInteraction interaction, @NotNull String alias, @NotNull TypeMap arguments, @NotNull Logger logger, @NotNull ISBDS sbds) {
        super(command, alias, arguments, logger, sbds);
        this.interaction = interaction;
        this.user = interaction.getUser();
    }

    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    public @NotNull User user() {
        return user;
    }

}
