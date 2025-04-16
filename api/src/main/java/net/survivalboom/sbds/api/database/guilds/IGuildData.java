package net.survivalboom.sbds.api.database.guilds;

import net.survivalboom.sbds.api.utils.NamespacedContainer;
import org.jetbrains.annotations.NotNull;

public interface IGuildData {

    long getId();

    @NotNull NamespacedContainer container();

    void save();

}
