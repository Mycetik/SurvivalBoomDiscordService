package net.survivalboom.sbds.core.utils.placeholders.wrappers;

import net.survivalboom.sbds.api.SbdsProvider;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModulePlaceholder implements IPlaceholders {

    private final IModule module;

    public ModulePlaceholder(@Nullable IModule module) {
        this.module = module;
    }

    @Override
    public @NotNull Placeholders placeholders() {

        if (module == null) {
            return Placeholders.of(
                    " ", "SBDS " + SbdsProvider.getInstance().getVersion(),
                    "name", "SBDS",
                    "version", SbdsProvider.getInstance().getVersion()
            );
        }

        return Placeholders.of(
                " ", module.toString(),
                "name", module.getName(),
                "version", module.getMeta().getVersion()
        );

    }

}
