package net.survivalboom.sbds.core.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.guilds.IGuildDataManager;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GuildData extends Valid implements IGuildData {

    private final GuildDataRecord record;

    private final GuildDataManager manager;


    public GuildData(@NotNull GuildDataRecord record, @NotNull GuildDataManager manager) {
        this.record = record;
        this.manager = manager;
    }

    // GUILD //

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public @NotNull Guild getGuild() {
        return null;
    }

    // TRANSLATION //

    @Override
    public @Nullable ITranslation getDefaultTranslation() {
        return null;
    }

    @Override
    public void setDefaultTranslation(@Nullable ITranslation translation) {

    }

    // DATA //

    @Override
    public @NotNull INamespacedDataContainer container() {
        return null;
    }

    @Override
    public void save() {

    }

    // MISC //


    @Override
    public @NotNull IGuildDataManager getManager() {
        return manager;
    }

    @Override
    public @NotNull CompletableFuture<Void> delete() {
        checkValid();
        return manager.deleteGuildData(this);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
