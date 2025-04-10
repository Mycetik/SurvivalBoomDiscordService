package net.survivalboom.sbds.api.database.guilds;

import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;

public interface IGuildData {

    long getId();

    @NotNull TypeMap data();

}
