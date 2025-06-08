package net.survivalboom.sbds.core.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.CommandExecutor;
import net.survivalboom.sbds.api.commands.argument.ArgumentResources;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.AbstractCommandManager;
import net.survivalboom.sbds.core.commands.cmds.common.StatusCommand;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SlashCommandManager extends AbstractCommandManager implements Listener, ISlashCommandManager {

    private final CommandInteractionManager commandInteractionManager;

    public SlashCommandManager(@NotNull SBDS sbds) {
        super("SlashCommandManager", sbds, true);
        this.commandInteractionManager = sbds.getCommandInteractionManager();
    }

    @Override
    protected void init0() {

        sbds.getEventManager().registerEvents0(null, this);

        commandInteractionManager.putGlobal(this::prepareGlobalCommandData);
        commandInteractionManager.putGuild(this::prepareGuildCommandData);

        registerCommand0(null, new StatusCommand(sbds).build(sbds, null));

    }

    @Override
    protected void shutdown0() {
        commands.clear();
        sbds.getEventManager().unregisterEvents(this);
    }

    @Override
    public void updateCommands() {
        commandInteractionManager.update();
    }


    private @NotNull List<CommandData> prepareGlobalCommandData() {
        return getRegisteredCommands().stream().filter(c -> c.command().global()).map(c -> createCommandData(c.command())).toList();
    }

    private @NotNull List<CommandData> prepareGuildCommandData() {
        return getRegisteredCommands().stream().filter(c -> c.command().guild()).map(c -> createCommandData(c.command())).toList();
    }


    private @NotNull CommandData createCommandData(@NotNull Command command) {

        String description = Objects.requireNonNullElse(command.description(), "Command has no description.");
        SlashCommandData commandData = Commands.slash(command.getName(), description);

        if (!command.hasSubcommands()) {
            commandData.addOptions(createCommandOptions(command));
        }

        else {
            addSlashSubCommands(commandData, command);
        }

        return commandData;

    }

    private List<OptionData> createCommandOptions(@NotNull Command command) {

        List<OptionData> out = new ArrayList<>();
        for (CommandArgument argument : command.arguments()) {

            OptionData optionData = new OptionData(argument.argument().getOptionType(), argument.name(), Objects.requireNonNullElse(argument.description(), "Option has no description."), argument.required());
            out.add(optionData);

        }

        return out;

    }

    private void addSlashSubCommands(@NotNull SlashCommandData slash, @NotNull Command command) {

        for (Command subcommand : command.subcommands()) {

            String name = subcommand.getName();
            String description = Objects.requireNonNullElse(subcommand.description(), "Subcommand has no description.");

            if (!subcommand.hasSubcommands()) {
                slash.addSubcommands(new SubcommandData(name, description).addOptions(createCommandOptions(subcommand)));
                continue;
            }


            SubcommandGroupData subcommandGroup = new SubcommandGroupData(name, description);
            for (Command subsubcommand : subcommand.subcommands()) {
                subcommandGroup.addSubcommands(new SubcommandData(subsubcommand.getName(), Objects.requireNonNullElse(subsubcommand.description(), "Subcommand has no description.")).addOptions(createCommandOptions(subsubcommand)));
            }

            slash.addSubcommandGroups(subcommandGroup);

        }

    }

    @EventHandler
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        String commandName = event.getName();

        RegisteredCommand registeredCommand = findByAlias(commandName);
        if (registeredCommand == null) {
            logger.warn("Something went wrong! Slash command with name `{}` does not exist in SlashCommandManager!", commandName);
            return;
        }

        Command baseCommand = registeredCommand.command();

        try {

            Command command = getCommand(baseCommand, event);

            if (!permissionCheck(command, event)) return;

            ArgumentResources resources = new ArgumentResources(sbds, TypeMap.empty(false));
            SlashCommandParser parser = new SlashCommandParser(command, resources, event.getInteraction());

            parser.parse();

            if (!parser.checkCount()) {
                throw new RuntimeException("Invalid argument count");
            }

            SlashExecutionInfo info = new SlashExecutionInfo(command, event.getInteraction(), commandName, parser.getArguments(), rootLogger, sbds);

            CommandExecutor executor = command.executor();
            executor.execute(info);

            if (!event.isAcknowledged()) {
                event.reply("Something went wrong. Looks like the executor `" + executor + "` refused to respond to the interaction.").queue();
                logger.error("Command executor of command /`{}` did not respond to the interaction. Are you sure you did it right?", commandName);
            }

        }

        catch (Throwable t) {
            logger.error("[{}] An internal error occurred while attempting to perform slash command /{}", event.getGuild() != null ? event.getGuild().getName() + ":" + event.getUser().getName() : event.getUser().getName(), commandName, t);
            if (!event.isAcknowledged()) messages.reply(event, "common.error", event.getUser()).withPlaceholders(Placeholders.of("{EXCEPTION}", t.toString())).queue();
            else messages.createActionMessage("common.error", event.getUser(), d -> event.getHook().editOriginal(MessageEditData.fromCreateData(d))).withPlaceholders(Placeholders.of("{EXCEPTION}", t.toString())).queue();
        }

    }

    private @NotNull Command getCommand(@NotNull Command baseCommand, @NotNull SlashCommandInteractionEvent event) {

        if (!baseCommand.hasSubcommands()) return baseCommand;

        String fullInput = event.getFullCommandName().substring(event.getName().length()).trim();
        String[] parts = fullInput.split(" ");

        Command command = baseCommand;
        for (String part : parts) {

             Command subcommand = command.subcommands().stream().filter(sc -> sc.getName().equals(part) || sc.aliases().contains(part)).findAny().orElse(null);
             if (subcommand == null) break;

             command = subcommand;

        }

        return command;

    }

    private boolean permissionCheck(@NotNull Command command, @NotNull SlashCommandInteractionEvent event) {

        String permission = command.permission();
        if (event.isFromGuild() && permission != null) {

            Guild guild = event.getGuild();
            Member member = event.getMember();

            assert member != null;
            assert guild != null;

            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                return true;
            }

            boolean hasPermission = permissionManager.hasPermission(guild.getIdLong(), member.getIdLong(), permission, command.defaultPermission());
            if (!hasPermission) {
                messages.reply(event.getInteraction(),"common.no-permission", event.getUser()).withPlaceholders(Placeholders.of("{PERMISSION}", permission)).queue();
                return false;
            }

        }

        return true;

    }

}
