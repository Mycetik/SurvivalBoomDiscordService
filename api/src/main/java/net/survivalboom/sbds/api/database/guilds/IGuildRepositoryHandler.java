package net.survivalboom.sbds.api.database.guilds;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGuildRepositoryHandler {

    @Nullable IGuildData getGuildData(long id);

    @Nullable IGuildData getGuildData(@NotNull Guild guild);


    @NotNull IGuildData createGuildData(long id);

    @NotNull IGuildData createGuildData(@NotNull Guild guild);


    boolean deleteGuildData(long id);

    boolean deleteGuildData(@NotNull Guild guild);


    void update(@NotNull IGuildData iGuildData);


}
