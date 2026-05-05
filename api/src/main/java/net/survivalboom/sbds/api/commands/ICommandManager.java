package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface ICommandManager<
        reg extends ICommandManager.IRegisteredCommand<reg, manager>,
        manager extends ICommandManager<reg, manager>
> extends IManager {

    //
    // REGISTRATION
    //

    // REG //

    @NotNull reg registerCommand(@NotNull IModule module, @NotNull Command command);

    default @NotNull reg registerCommand(@NotNull ModuleMain main, @NotNull Command command) {
        return registerCommand(main.getModule(), command);
    }


    default @NotNull reg registerCommand(@NotNull IModule module, @NotNull CommandBase command) {
        return registerCommand(module, command.build());
    }

    default @NotNull reg registerCommand(@NotNull ModuleMain main, @NotNull CommandBase command) {
        return registerCommand(main, command.build());
    }

    // UNREG //

    boolean unregisterCommand(@NotNull reg registration);

    default @Nullable reg unregisterCommand(@NotNull NamespacedKey key) {

        var reg = getCommand(key);
        if (reg == null) {
            return null;
        }

        unregisterCommand(reg);

        return reg;

    }

    default @Nullable reg unregisterCommand(@NotNull String name) {

        var reg = getCommand(name);
        if (reg == null) {
            return null;
        }

        unregisterCommand(reg);

        return reg;

    }

    // GETTERS //

    @Nullable reg getCommand(@NotNull NamespacedKey key);

    default @Nullable reg getCommand(@NotNull String name) {
        return getCommands().stream()
                .filter(command -> command.getCommand().getName().equals(name))
                .findAny()
                .orElse(null);
    }

    @NotNull List<reg> getCommands();

    default @Nullable reg getByAlias(@NotNull String alias) {
        return getCommands().stream().filter(reg -> reg.getCommand().getAliases().contains(alias)).findAny().orElse(null);
    }

    //
    // MISC
    //

    @NotNull ISBDS getSbds();

    //
    // REGISTERED COMMAND
    //

    interface IRegisteredCommand<it extends IRegisteredCommand<it, manager>, manager extends ICommandManager<it, manager>> {

        @NotNull Registration<it> getRegistration();

        @NotNull Command getCommand();

        @NotNull manager getManager();

    }

}
