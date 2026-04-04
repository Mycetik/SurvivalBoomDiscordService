package net.survivalboom.sbds.core.commands;

import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ICommandManager;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.messages.Messages;
import net.survivalboom.sbds.core.modules.Module;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.permissions.PermissionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public abstract class AbstractCommandManager extends Manager implements ICommandManager {

    protected final List<RegisteredCommand> commands = new ArrayList<>();

    protected final String name;


    protected final SBDS sbds;

    protected final PermissionManager permissionManager;

    protected final Messages messages;


    protected final Logger logger;

    protected final Logger rootLogger;


    protected final boolean subcommandsAllowed;


    public AbstractCommandManager(@NotNull String name, @NotNull SBDS sbds, boolean subcommandsAllowed) {

        this.name = name;
        this.sbds = sbds;

        this.permissionManager = sbds.getPermissionManager();
        this.messages = sbds.getMessages();

        this.rootLogger = sbds.getLogger();
        this.logger = LoggerFactory.getLogger(name);

        this.subcommandsAllowed = subcommandsAllowed;

    }


    @Override
    public @NotNull RegisteredCommand registerCommand(@NotNull IModule module, @NotNull Command command) {
        Objects.requireNonNull(module, "module == null");
        return registerCommand0(module, command);
    }

    public @NotNull RegisteredCommand registerCommand0(@Nullable IModule imodule, @NotNull Command command) {

        checkValid();

        Objects.requireNonNull(command, "command == null");

        if (getRegisteredCommand(command.getName()) != null) throw new IllegalArgumentException("Command with name `" + command.getName() + "` already registered");

        if (!subcommandsAllowed && command.hasSubcommands()) throw new IllegalArgumentException(name + "does not support subcommands");

        RegisteredCommand registeredCommand;
        if (imodule != null) {
            Module module = sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module attempted to register a command");
            module.getRegistration().add(name + "-" + command.getName(), () -> unregisterCommand(command));

            registeredCommand = new RegisteredCommand(module, command);

        }

        else {
            registeredCommand = new RegisteredCommand(null, command);
        }

        commands.add(registeredCommand);

        return registeredCommand;

    }


    @Override
    public void unregisterCommand(@NotNull Command command) {
        checkValid();
        Objects.requireNonNull(command, "command == null");
        commands.removeIf(c -> c.command().equals(command));
    }


    @Override
    public @Nullable RegisteredCommand getRegisteredCommand(@NotNull String name) {
        checkValid();
        return commands.stream().filter(rc -> rc.command().getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public @Nullable RegisteredCommand findByAlias(@NotNull String alias) {
        checkValid();
        return commands.stream().filter(rc -> rc.command().getName().equals(alias) || rc.command().aliases().contains(alias)).findFirst().orElse(null);
    }

    @Override
    public @Nullable RegisteredCommand findByBase(@NotNull CommandBase base) {
        checkValid();
        return commands.stream().filter(rc -> base.equals(rc.command().origin())).findAny().orElse(null);
    }

    @Override
    public @NotNull List<RegisteredCommand> getRegisteredCommands() {
        checkValid();
        return new ArrayList<>(commands);
    }

}
