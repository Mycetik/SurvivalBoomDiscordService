package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public abstract class ModuleMain {

    private ISBDS sbds;

    private IModule module;

    //
    // LIFECYCLE (will be overridden in module's class)
    //

    @ApiStatus.OverrideOnly
    public void onLoad() throws Exception {}

    @ApiStatus.OverrideOnly
    public void onUnload() throws Exception {}


    @ApiStatus.OverrideOnly
    public void onEnable() throws Exception {}

    @ApiStatus.OverrideOnly
    public void onDisable() throws Exception {}

    //
    // INIT
    //

    @ApiStatus.Internal
    public final void init(@NotNull IModule module, @NotNull ISBDS sbds) {

        if (this.module != null) {
            throw new RuntimeException("Сука, ну написано же для таких долбоебов как ты 'Внутреннее API'. Ты слишком тупой чтобы использовать его, понимаешь? Не для тебя оно было сделано.");
        }

        this.module = module;
        this.sbds = sbds;

    }


    public final @NotNull IModule getModule() {
        return module;
    }

    public final @NotNull ISBDS getSbds() {
        return sbds;
    }

    //
    // GETTERS
    //

    public @NotNull String getName() {
        return getModule().getName();
    }

    public @Nullable ModuleFile getFile() {
        return getModule().getFile();
    }

    public @NotNull File getDataFolder() {
        return getModule().getDataFolder();
    }

    public @NotNull IModuleManager getModuleManager() {
        return getSbds().getModuleManager();
    }

    public @NotNull IDatabase getDatabase() {
        return getSbds().getDatabase();
    }

    public @NotNull Logger getLogger() {
        return getModule().getLogger();
    }

    public @NotNull ModuleMeta getMeta() {
        return getModule().getMeta();
    }

    //
    // REGISTRATIONS
    //

    // <-- пусто!

    //
    // CONFIG
    //

    public void checkFiles(@NotNull Map<String, String> map) {
        Objects.requireNonNull(map, "map == null");
        CommonUtils.checkFiles(this.getClass(), getModule().getDataFolder(), map, null);
    }

}
