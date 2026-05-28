package net.survivalboom.sbds.api.commands.slash;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutionInfo;
import net.survivalboom.sbds.api.interaction.CanModal;
import net.survivalboom.sbds.api.interaction.CanReply;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.jetbrains.annotations.NotNull;

public class SlashExecutionInfo extends CommandExecutionInfo<ISlashCommandManager.IRegisteredSlashCommand, ISlashCommandManager> implements CanReply<SlashCommandInteraction>, CanModal<SlashCommandInteraction> {

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
    public @NotNull SlashCommandInteraction interaction() {
        return interaction;
    }

    public boolean hasPermission(@NotNull String permission) {
        return sbds.getPermissionManager().hasPermission(member(), permission, false);
    }

}
