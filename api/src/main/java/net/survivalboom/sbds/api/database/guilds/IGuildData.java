package net.survivalboom.sbds.api.database.guilds;

import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGuildData {

    long getId();

    @NotNull NamespacedContainer container();

    @Nullable ITranslation translation();

    void save();

}
