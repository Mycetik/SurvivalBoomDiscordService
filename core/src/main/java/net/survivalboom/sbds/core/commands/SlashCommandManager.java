package net.survivalboom.sbds.core.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.builtin.StatusCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SlashCommandManager extends AbstractCommandManager implements Listener, ISlashCommandManager {

    public SlashCommandManager(@NotNull SBDS sbds) {
        super("SlashCommandManager", sbds, false);
    }

    @Override
    protected void init0() {

        sbds.getEventManager().registerEvents0(null, this);

        registerCommand0(null, new StatusCommand().build());
    }

    @Override
    protected void shutdown0() {
        commands.clear();
        sbds.getEventManager().unregisterEvents(this);
    }

    @Override
    public void updateCommands() {
        sbds.getBot().getGuilds().forEach(guild -> guild.updateCommands().addCommands(prepareCommandData()).queue());
    }


    private @NotNull List<SlashCommandData> prepareCommandData() {

        List<SlashCommandData> out = new ArrayList<>();

        for (RegisteredCommand registeredCommand : getRegisteredCommands()) {
            out.add(createCommandData(registeredCommand.command()));
        }

        return out;

    }


    private @NotNull SlashCommandData createCommandData(@NotNull Command command) {

        String description = Objects.requireNonNullElse(command.description(), "Command has no description.");
        SlashCommandData commandData = Commands.slash(command.getName(), description);

        for (CommandArgument argument : command.arguments()) {

            OptionData optionData = new OptionData(argument.argument().getOptionType(), argument.name(), Objects.requireNonNullElse(argument.description(), "Option has no description."), argument.required());

            commandData.addOptions(optionData);

        }

        return commandData;

    }

    @EventHandler
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String commandName = event.getName();

        RegisteredCommand registeredCommand = findByAlias(commandName);
        if (registeredCommand == null) {
            logger.error("Something went wrong! Slash command with name {} does not exist in SlashCommandManager!", commandName);
            return;
        }

        Command command = registeredCommand.command();

        try {

            ArgumentResources resources = new ArgumentResources(sbds, new TypeMap());

            SlashCommandParser parser = new SlashCommandParser(command, resources, event.getInteraction());

            parser.parse();

            if (!parser.checkCount()) {
                event.reply("Ебать, пизда, ты все сломал.").queue();
                return;
            }

            SlashExecutionInfo info = new SlashExecutionInfo(command, event.getInteraction(), commandName, parser.getArguments(), rootLogger, sbds);

            command.executor().execute(info);

        }

        catch (Throwable t) {
            logger.error("[{}:{}] An internal error occurred while attempting to perform slash command /{}.", event.getGuild().getName(), event.getUser().getName(), commandName, t);
            event.reply("Ебать, пизда, ты все сломал.").queue();
        }

    }

}
