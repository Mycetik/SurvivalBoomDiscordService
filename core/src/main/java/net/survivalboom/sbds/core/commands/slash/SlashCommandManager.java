package net.survivalboom.sbds.core.commands.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.ArgumentScope;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandArgument;
import net.survivalboom.sbds.api.commands.CommandExecutor;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.AbstractCommandManager;
import net.survivalboom.sbds.core.commands.cmds.common.StatusCommand;
import net.survivalboom.sbds.core.commands.parser.SlashCommandParser;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import net.survivalboom.sbds.core.interaction.command.CommandLocalizator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO Зробити так щоб команди компілювались в CommandData лише раз, тільки при реєстрації або ініціалізації менеджера.
public class SlashCommandManager extends AbstractCommandManager implements Listener, ISlashCommandManager {

    private final CommandInteractionManager commandInteractionManager;

    private final CommandLocalizator localizator;


    public SlashCommandManager(@NotNull SBDS sbds) {
        super("SlashCommandManager", sbds, true);
        this.commandInteractionManager = sbds.getCommandInteractionManager();
        this.localizator = new CommandLocalizator(sbds.getTranslationManager());
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

        String description = Objects.requireNonNullElse(command.description(), "-");
        SlashCommandData commandData = Commands.slash(command.getName(), description);

        commandData.setLocalizationFunction(localizator.createLocalizationFunction(command));

        if (!command.hasSubcommands()) {
            commandData.addOptions(createCommandOptions(command));
        }

        else {
            addSlashSubCommands(commandData, command);
        }

        return commandData;

    }

//    private @Nullable String getLocalizationKey(@NotNull Command command, @NotNull String request) {
//
//        String[] parts = request.split("\\.");
//        int lastIndex = parts.length - 1;
//        int lastInformativeIndex = lastIndex - 1;
//        String type = parts[lastIndex];
//
//        if (parts[lastInformativeIndex].equals(command.getName())) {
//            if (type.equals("name")) return null;
//            String translationKey = command.translationKey();
//            return translationKey != null ? translationKey + "." + type : null;
//        }
//
//        Command targetCommand = command;
//        int index = 0;
//        for (int i = 1; i < parts.length && i < 4; i++) {
//
//            index = i;
//
//            String part = parts[i];
//            Command cmd = targetCommand.subcommands().stream().filter(c -> c.getName().equals(part)).findAny().orElse(null);
//            if (cmd == null) break;
//
//            targetCommand = cmd;
//
//        }
//
//        String argumentTarget = parts[index];
//        CommandArgument argument = targetCommand.arguments().stream().filter(a -> a.name().equals(argumentTarget)).findAny().orElse(null);
//        if (argument == null) {
//            return null;
//        }
//
//        String argumentTranslationKey = argument.translationKey();
//        if (argumentTranslationKey == null) {
//            return null;
//        }
//
//        if (index == lastInformativeIndex) {
//            return argumentTranslationKey;
//        }
//
//        index++;
//
//        String additionalTranslationKey = parts[index];
//
//        return argumentTranslationKey + "." + additionalTranslationKey;
//
//    }


    @EventHandler
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        commandInteractionManager.updateGuild(event.getGuild());
    }

    @EventHandler
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        if (!sbds.isReady()) return;

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

            Argument.ArgumentResources resources = new Argument.ArgumentResources(sbds, TypeMap.empty(false));
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
            if (!event.isAcknowledged()) messages.reply(event, "sbds.error", event.getUser()).withPlaceholders(Placeholders.of("{EXCEPTION}", t.toString())).queue();
            else messages.createActionMessage("sbds.error", event.getUser(), d -> event.getHook().editOriginal(MessageEditData.fromCreateData(d))).withPlaceholders(Placeholders.of("{EXCEPTION}", t.toString())).queue();
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

            Member member = event.getMember();

            assert member != null;

            boolean hasPermission = permissionManager.hasPermission(member, permission, command.defaultPermission());
            if (!hasPermission) {
                messages.reply(event.getInteraction(),"sbds.no-permission", event.getUser()).withPlaceholders(Placeholders.of("{PERMISSION}", permission)).queue();
                return false;
            }

        }

        return true;

    }

}
