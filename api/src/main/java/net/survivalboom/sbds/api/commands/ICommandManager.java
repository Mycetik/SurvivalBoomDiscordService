package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommandManager {

    void registerCommand(@NotNull IModule module, @NotNull Command command);

    default void registerCommand(@NotNull IModule module, @NotNull CommandBase command) {
        registerCommand(module, command.build(module));
    }

    default void registerCommand(@NotNull ModuleMain main, @NotNull Command command) {
        registerCommand(main.getModule(), command);
    }

    default void registerCommand(@NotNull ModuleMain main, @NotNull CommandBase command) {
        registerCommand(main, command.build(main.getModule()));
    }

    void unregisterCommand(@NotNull Command command);


    @Nullable RegisteredCommand getRegisteredCommand(@NotNull String name);

    @NotNull List<RegisteredCommand> getRegisteredCommands();

    @Nullable RegisteredCommand findByAlias(@NotNull String alias);

    @Nullable RegisteredCommand findByBase(@NotNull CommandBase base);


    record RegisteredCommand(@Nullable IModule registrar, @NotNull Command command) {}

}
