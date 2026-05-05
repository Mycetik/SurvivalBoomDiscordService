package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.commands.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface StringCommand extends CommandExecutor<StringExecutionInfo> {

    // DM //

    boolean dmGlobal();

    void setDMGlobal(boolean value);


    @NotNull List<Guild> getDMs();

    void setDMs(@Nullable Collection<Guild> collection);

    void addDMs(@NotNull Guild guild);

    void removeDMs(@NotNull Guild guild);

    // GUILD //

    boolean guildGlobal();

    void setGuildGlobal(boolean value);


    @NotNull List<Guild> getGuilds();

    void setGuilds(@Nullable Collection<Guild> collection);

    void addGuild(@NotNull Guild guild);

    void removeGuild(@NotNull Guild guild);

}
