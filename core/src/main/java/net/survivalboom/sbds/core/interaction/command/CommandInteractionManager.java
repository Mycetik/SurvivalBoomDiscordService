package net.survivalboom.sbds.core.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.survivalboom.sbds.api.commands.*;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.interaction.command.ICommandInteractionManager;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.utils.InternalUpdateQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommandInteractionManager extends Manager implements ICommandInteractionManager {

    private static final Logger log = LoggerFactory.getLogger(CommandInteractionManager.class.getSimpleName());


    private final SBDS sbds;

    private final CommandLocalizator localizator;

    private final Map<ICommandManager.IRegisteredCommand<?, ?>, RegisteredCommandData> registeredCommands = new HashMap<>();


    private final InternalUpdateQueue queue;


    public CommandInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.localizator = new CommandLocalizator(sbds.getTranslationManager());
        this.queue = new InternalUpdateQueue(this::updateGlobal, "CommandInteractionManager-UpdateQueue", 1000, sbds.getScheduler());
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {
        this.queue.init();
    }

    @Override
    protected void shutdown0() {
        this.queue.shutdown();
        registeredCommands.clear();
    }

    //
    // REG/UNREG
    //

    public synchronized @NotNull RegisteredCommandData registerCommand(
            @NotNull ICommandManager.IRegisteredCommand<?, ?> reg,
            @NotNull Type type
    ) {

        Objects.requireNonNull(reg, "reg == null");
        Objects.requireNonNull(type, "type == null");
        checkValid();

        if (registeredCommands.containsKey(reg)) {
            throw new IllegalArgumentException("That command object is already registered");
        }

        Command command = reg.getCommand();

        CommandData commandData = switch (type) {
            case SLASH -> createSlashCommandData(command);
            case USER, MESSAGE -> createContextCommandData(command, type);
            default -> throw new IllegalArgumentException("Invalid command type `" + type + "`");
        };

        RegisteredCommandData registeredCommandData = new RegisteredCommandData(reg, commandData, type, this);
        registeredCommands.put(reg, registeredCommandData);

        queue.requestUpdate();

        return registeredCommandData;

    }

    public synchronized void unregisterCommand(@NotNull ICommandManager.IRegisteredCommand<?, ?> command) {

        Objects.requireNonNull(command, "command == null");
        checkValid();

        if (!registeredCommands.containsKey(command)) {
            throw new IllegalArgumentException("Command object `" + command + "` is not registered");
        }

        registeredCommands.remove(command);

        queue.requestUpdate();

    }

    //
    // UPDATE
    //

    @Override
    public void requestGlobalUpdate() {
        checkValid();
        this.queue.requestUpdate();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateGlobal() {

        JDA bot = sbds.getBot();

        List<CommandData> global = registeredCommands.values().stream()
                .filter(command -> command.isGlobal)
                .map(command -> command.commandData)
                .toList();

        log.info("Updating {} commands...", registeredCommands.size());

        bot.updateCommands().addCommands(global).queue();
        bot.getGuilds().forEach(guild -> updateGuild(guild, true));

    }

    @Override
    public synchronized void updateGuild(@NotNull Guild guild) {
        updateGuild(guild, false);
    }

    private synchronized void updateGuild(@NotNull Guild guild, boolean silent) {

        Objects.requireNonNull(guild, "guild == null");
        checkValid();

        List<CommandData> list = registeredCommands.values().stream()
                .filter(command -> command.isGuildGlobal || command.guildRegistrations.contains(guild))
                .map(command -> command.commandData)
                .toList();

        if (!silent) {
            log.info("Updating commands for `{}`. Registering {} commands.", guild, list.size());
        }

        guild.updateCommands().addCommands(list).queue();

    }

    //
    // COMMAND DATA
    //

    // CONTEXT //

    private @NotNull CommandData createContextCommandData(@NotNull Command command, @NotNull net.dv8tion.jda.api.interactions.commands.Command.Type type) {
        return Commands.context(type, command.getName());
    }

    // SLASH //

    private @NotNull SlashCommandData createSlashCommandData(@NotNull Command command) {

        String description = Objects.requireNonNullElse(command.getDescription(), "-");
        SlashCommandData commandData = Commands.slash(command.getName(), description);

        commandData.setLocalizationFunction(localizator.createLocalizationFunction(command));

        List<CommandArgument> subcommandArguments = command.getArguments().stream()
                .filter(argument -> argument.argument() instanceof SubCommandArgument)
                .toList();

        if (subcommandArguments.size() > 1) {
            throw new IllegalArgumentException("Provided command has more than 1 subcommand");
        }

        boolean hasSubCommands = !subcommandArguments.isEmpty();

        if (hasSubCommands) {
            List<Command> subcommands = ((SubCommandArgument) subcommandArguments.getFirst().argument()).getSubcommands();
            addSlashSubCommands(commandData, subcommands);
        }

        else {
            commandData.addOptions(createSlashCommandOptions(command));
        }

        return commandData;

    }

    private void addSlashSubCommands(@NotNull SlashCommandData slash, @NotNull List<Command> subcommands) {

        for (Command command : subcommands) {

            String name = command.getName();
            String description = Objects.requireNonNullElse(command.getDescription(), "- ");

            List<CommandArgument> subcommandArguments = command.getArguments().stream()
                    .filter(argument -> argument.argument() instanceof SubCommandArgument)
                    .toList();

            if (subcommandArguments.size() > 1) {
                throw new IllegalArgumentException("Provided sub-command`" + name + "` has more than 1 subcommand");
            }

            boolean hasSubCommands = !subcommandArguments.isEmpty();

            if (!hasSubCommands) {
                slash.addSubcommands(new SubcommandData(name, description).addOptions(createSlashCommandOptions(command)));
                continue;
            }

            List<Command> subCommands = ((SubCommandArgument) subcommandArguments.getFirst().argument()).getSubcommands();
            SubcommandGroupData subcommandGroup = new SubcommandGroupData(name, description);

            for (Command subcommand : subcommands) {
                subcommandGroup.addSubcommands(new SubcommandData(subcommand.getName(), Objects.requireNonNullElse(subcommand.getDescription(), "- ")).addOptions(createSlashCommandOptions(subcommand)));
            }

            slash.addSubcommandGroups(subcommandGroup);

        }

    }

    private List<OptionData> createSlashCommandOptions(@NotNull Command command) {

        List<OptionData> out = new ArrayList<>();
        for (CommandArgument argument : command.getArguments()) {

            if (!argument.scopes().contains(ArgumentScope.SLASH)) {
                continue;
            }

            OptionData optionData = argument.argument().createOptionData(argument);
            out.add(optionData);

        }

        return out;

    }

    public static class RegisteredCommandData implements IRegisteredCommandData {

        private final CommandData commandData;

        private final ICommandManager.IRegisteredCommand<?, ?> command;

        private final Type type;

        private final CommandInteractionManager manager;


        private boolean isGlobal = false;

        private boolean isGuildGlobal = true;


        private final List<Guild> guildRegistrations = new ArrayList<>();


        public RegisteredCommandData(
                @NotNull ICommandManager.IRegisteredCommand<?, ?> command,
                @NotNull CommandData commandData,
                @NotNull Type type,
                @NotNull CommandInteractionManager manager
        ) {
            this.command = command;
            this.commandData = commandData;
            this.type = type;
            this.manager = manager;
        }

        // INFO //

        @Override
        public @NotNull CommandData getCommandData() {
            return commandData;
        }

        @Override
        public @NotNull ICommandManager.IRegisteredCommand<?, ?> getCommand() {
            return command;
        }

        @Override
        public @NotNull ICommandInteractionManager getManager() {
            return manager;
        }

        @Override
        public @NotNull Type getType() {
            return type;
        }

        // GLOBAL //

        @Override
        public boolean isGlobal() {
            return isGlobal;
        }

        @Override
        public void setGlobal(boolean value) {

            this.isGlobal = value;

            if (value) {
                this.isGuildGlobal = false;
                this.guildRegistrations.clear();
            }

        }

        // GUILD //

        @Override
        public boolean isGuildGlobal() {
            return isGuildGlobal;
        }

        @Override
        public void setGuildGlobal(boolean value) {

            this.isGuildGlobal = value;

            if (value) {
                this.isGlobal = false;
                this.guildRegistrations.clear();
            }

        }



        @Override
        public @NotNull List<Guild> getGuildRegistrations() {
            return new ArrayList<>(guildRegistrations);
        }

        @Override
        public void setGuildRegistrations(@Nullable Collection<Guild> collection) {

            this.guildRegistrations.clear();

            if (collection == null) {
                return;
            }

            this.isGuildGlobal = false;
            this.isGlobal = false;

            this.guildRegistrations.addAll(collection);

        }

        @Override
        public void addGuildRegistration(@NotNull Guild guild) {

            Objects.requireNonNull(guild, "guild == null");

            this.isGuildGlobal = false;
            this.isGlobal = false;

            this.guildRegistrations.add(guild);

        }

        @Override
        public void removeGuildRegistration(@NotNull Guild guild) {
            this.guildRegistrations.remove(guild);
        }

    }

}
