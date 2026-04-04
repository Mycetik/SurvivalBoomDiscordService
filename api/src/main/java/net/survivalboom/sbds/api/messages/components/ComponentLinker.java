package net.survivalboom.sbds.api.messages.components;

import org.jetbrains.annotations.NotNull;

public interface ComponentLinker {

    @NotNull String link(@NotNull ComponentTemplate component);

}
