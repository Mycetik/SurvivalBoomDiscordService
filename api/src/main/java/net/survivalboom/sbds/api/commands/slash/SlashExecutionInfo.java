package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ExecutionInfo;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
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

}
