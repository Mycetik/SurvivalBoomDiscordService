package net.survivalboom.sbds.api.interaction.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.survivalboom.sbds.api.commands.ICommandManager;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ICommandInteractionManager extends IManager {

    void requestGlobalUpdate();

    void updateGuild(@NotNull Guild guild);

    interface IRegisteredCommandData {

        @NotNull CommandData getCommandData();

        @NotNull ICommandManager.IRegisteredCommand<?, ?> getCommand();

        @NotNull Command.Type getType();

        @NotNull ICommandInteractionManager getManager();

        // GLOBAL //

        boolean isGlobal();

        void setGlobal(boolean value);

        // GUILD //

        boolean isGuildGlobal();

        void setGuildGlobal(boolean value);


        @NotNull List<Guild> getGuildRegistrations();

        void setGuildRegistrations(@Nullable Collection<Guild> collection);

        void addGuildRegistration(@NotNull Guild guild);

        void removeGuildRegistration(@NotNull Guild guild);

    }

}
