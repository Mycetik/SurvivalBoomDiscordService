package net.survivalboom.sbds.api.commands.argument;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

public record ArgumentResources(@NotNull ISBDS sbds, @NotNull TypeMap map) {}
