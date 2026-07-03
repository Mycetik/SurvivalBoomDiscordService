package net.survivalboom.sbds.core.commands;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.ICommandManager;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.interaction.command.ICommandInteractionManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.registrations.RegistrationManager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.messages.Messages;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.permissions.PermissionManager;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public abstract class AbstractCommandManager<
        reg extends ICommandManager.IRegisteredCommand<reg, manager>,
        manager extends ICommandManager<reg, manager>
> extends Manager implements ICommandManager<reg, manager>, RegistrationManager.Callback<reg> {

    protected final SBDS sbds;

    protected final PermissionManager permissionManager;

    protected final Messages messages;


    protected final InternalRegistrationManager<reg> registry;


    protected final Logger logger;

    protected final Logger rootLogger;


    public AbstractCommandManager(
            @NotNull SBDS sbds
    ) {

        this.sbds = sbds;

        this.permissionManager = sbds.getPermissionManager();
        this.messages = sbds.getMessages();

        this.registry = new InternalRegistrationManager<>(this, this, sbds.getRegistrationRegistry());

        this.rootLogger = sbds.getLogger();
        this.logger = LoggerFactory.getLogger(getManagerName());

    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {
        registry.init();
    }

    @Override
    protected void shutdown0() {
        registry.shutdown();
    }

    @Override
    public @NotNull ISBDS getSbds() {
        return sbds;
    }

    //
    // REGISTRATION
    //

    // REG //

    @Override
    public @NotNull reg registerCommand(@NotNull IModule module, @NotNull Command command) {
        Objects.requireNonNull(module, "module == null");
        return registerCommand0(module, command);
    }

    @SuppressWarnings("unchecked")
    public @NotNull reg registerCommand0(@Nullable IModule module, @NotNull Command command) {

        Objects.requireNonNull(command, "command == null");
        checkValid();

        for (var reg : registry.getRegisteredObjects()) {

            Command cmd = reg.getCommand();
            if (cmd.getName().equals(command.getName())) {
                throw new IllegalStateException("Command with name `" + command.getName() + "` already exists (" + reg.getRegistration().key() + ")");
            }


            String alias = cmd.getAliases().stream()
                    .filter(a -> command.getAliases().contains(a))
                    .findAny()
                    .orElse(null);

            if (alias != null) {
                throw new IllegalStateException("Command with alias `" + alias + "` already exists (" + reg.getRegistration().key() + ")");
            }

        }

        var cmdReg = createCommandReg(command);
        var oinkOinkOink = (RegisteredCommand<reg, manager>) cmdReg;

        oinkOinkOink.registration = registry.register0(module, command.getName(), cmdReg);

        return cmdReg;

    }

    public @NotNull reg registerCommand0(@Nullable IModule module, @NotNull CommandBase command) {
        return registerCommand0(module, command.build());
    }

    // UNREG //

    @Override
    public boolean unregisterCommand(@NotNull reg registration) {
        return registry.unregister(registration) != null;
    }

    //
    // GETTERS
    //

    @Override
    public @NotNull List<reg> getCommands() {
        return registry.getRegisteredObjects();
    }

    @Override
    public @Nullable reg getCommand(@NotNull NamespacedKey key) {
        return registry.getRegistrationAsObject(key);
    }


    //
    // ABSTRACT
    //

    protected abstract @NotNull reg createCommandReg(@NotNull Command command);

    //
    // REG
    //

    public static abstract class RegisteredCommand<
            it extends IRegisteredCommand<it, manager>,
            manager extends ICommandManager<it, manager>
    > implements IRegisteredCommand<it, manager> {

        protected final manager manager;

        protected final Command command;

        protected Registration<it> registration;


        public RegisteredCommand(@NotNull manager manager, @NotNull Command command) {
            this.manager = manager;
            this.command = command;
        }


        @Override
        public @NotNull Registration<it> getRegistration() {
            return registration;
        }

        @Override
        public @NotNull Command getCommand() {
            return command;
        }

        @Override
        public @NotNull manager getManager() {
            return manager;
        }

    }

    public static abstract class RegisteredInteractionCommand<
            it extends IRegisteredInteractionCommand<it, manager>,
            manager extends ICommandManager<it, manager>
    > extends RegisteredCommand<it, manager> implements IRegisteredInteractionCommand<it, manager> {

        public List<ICommandInteractionManager.IRegisteredCommandData> commandData;

        public RegisteredInteractionCommand(@NotNull manager manager, @NotNull Command command) {
            super(manager, command);
        }

        @Override
        public @NotNull List<ICommandInteractionManager.IRegisteredCommandData> getCommandData() {
            return new ArrayList<>(commandData);
        }

    }

}
