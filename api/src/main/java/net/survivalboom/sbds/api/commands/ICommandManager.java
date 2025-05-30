package net.survivalboom.sbds.api.commands;

import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommandManager {

    @NotNull RegisteredCommand registerCommand(@NotNull IModule module, @NotNull Command command);

    default @NotNull RegisteredCommand registerCommand(@NotNull IModule module, @NotNull CommandBase command) {
        return registerCommand(module, command.build(module.getSbds(), module));
    }

    default @NotNull RegisteredCommand registerCommand(@NotNull ModuleMain main, @NotNull Command command) {
        return registerCommand(main.getModule(), command);
    }

    default @NotNull RegisteredCommand registerCommand(@NotNull ModuleMain main, @NotNull CommandBase command) {
        return registerCommand(main, command.build(main.getSbds(), main.getModule()));
    }

    void unregisterCommand(@NotNull Command command);


    @Nullable RegisteredCommand getRegisteredCommand(@NotNull String name);

    @NotNull List<RegisteredCommand> getRegisteredCommands();

    @Nullable RegisteredCommand findByAlias(@NotNull String alias);

    @Nullable RegisteredCommand findByBase(@NotNull CommandBase base);


    record RegisteredCommand(@Nullable IModule registrar, @NotNull Command command) {}

}
