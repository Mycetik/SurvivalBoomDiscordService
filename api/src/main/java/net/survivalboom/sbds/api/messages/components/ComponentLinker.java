package net.survivalboom.sbds.api.messages.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentLinker {

    @NotNull String link(@NotNull MessageInteractableComponentTemplate<?> component);

    static @NotNull String stLink(@Nullable ComponentLinker linker, @NotNull MessageInteractableComponentTemplate<?> template) {

        if (linker == null || template.isStatic()) {

            String name = template.getName();
            if (name == null) {
                throw new IllegalArgumentException("This component does not have its name. We cant link it!");
            }

            return name;

        }

        return linker.link(template);

    }

}
