package net.survivalboom.sbds.api.modules;

import org.jetbrains.annotations.NotNull;

public abstract class ModuleMain {

    private IModule module = null;

    //
    // LIFECYCLE (will be overridden in module's class)
    //

    public void onLoad() {}

    public void onUnload() {}


    public void onEnable() {}

    public void onDisable() {}

    //
    // INIT
    //

    public final void init(@NotNull IModule module) {
        if (this.module != null) throw new RuntimeException("Сука, ну написано же для таких долбоебов как ты 'Внутреннее API'. Ты слишком тупой чтобы использовать его, понимаешь? Не для тебя оно было сделано.");
        this.module = module;
    }


    public @NotNull IModule getModule() {
        return module;
    }

}
