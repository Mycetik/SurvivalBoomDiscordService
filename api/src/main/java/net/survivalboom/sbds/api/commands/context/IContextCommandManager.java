package net.survivalboom.sbds.api.commands.context;

import net.survivalboom.sbds.api.commands.base.ContextCommandBase;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IContextCommandManager {

    @NotNull RegisteredContextCommand registerContextCommand(@NotNull IModule module, @NotNull ContextCommand command);

    default @NotNull RegisteredContextCommand registerContextCommand(@NotNull IModule module, @NotNull ContextCommandBase base) {
        return registerContextCommand(module, base.build());
    }

    default @NotNull RegisteredContextCommand registerContextCommand(@NotNull ModuleMain main, @NotNull ContextCommand command) {
        return registerContextCommand(main.getModule(), command);
    }

    default @NotNull RegisteredContextCommand registerContextCommand(@NotNull ModuleMain main,  @NotNull ContextCommandBase base) {
        return registerContextCommand(main, base.build());
    }

    void unregisterContextCommand(@NotNull String name);


    interface RegisteredContextCommand {

        @Nullable IModule module();

        @NotNull ContextCommand command();

    }

}
