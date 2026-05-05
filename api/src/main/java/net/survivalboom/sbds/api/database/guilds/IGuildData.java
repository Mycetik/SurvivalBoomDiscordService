package net.survivalboom.sbds.api.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.translations.ITranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IGuildData {

    // GUILD //

    long getId();

    @NotNull Guild getGuild();

    // TRANSLATION //

    @Nullable ITranslation getDefaultTranslation();

    void setDefaultTranslation(@Nullable ITranslation translation);

    // DATA //

    @NotNull INamespacedDataContainer container();

    void save();

    // MISC //

    @NotNull IGuildDataManager getManager();

    @NotNull CompletableFuture<Void> delete();

}
