package net.survivalboom.sbds.api.messages.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentLinker {

    @NotNull String link(@NotNull ComponentTemplate component);

    static @NotNull String stLink(@Nullable ComponentLinker linker, @NotNull ComponentTemplate template) {

        if (linker == null || template.isStatic()) {
            return template.getName();
        }

        return linker.link(template);

    }

}
