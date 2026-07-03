package net.survivalboom.sbds.api.commands.string;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.commands.ICommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface IStringCommandManager extends ICommandManager<IStringCommandManager.IRegisteredStringCommand, IStringCommandManager> {

    String STRING_COMMAND_PREFIX = "!"; // TODO: Можливо варто додати краще опцію у конфіг, ніж хардкодити?

    interface IRegisteredStringCommand extends ICommandManager.IRegisteredCommand<IRegisteredStringCommand, IStringCommandManager> {

        //
        // GUILD
        //

        // GUILD GLOBAL //

        @NotNull IRegisteredStringCommand setGuildGlobal(boolean v);

        boolean isGuildGlobal();

        // PER GUILD //

        @NotNull IRegisteredStringCommand setGuildRegistrations(@Nullable Collection<Guild> guilds);

        @NotNull IRegisteredStringCommand addGuildRegistration(@NotNull Guild guild);

        @NotNull IRegisteredStringCommand removeGuildRegistration(@NotNull Guild guild);

        @NotNull List<Guild> getGuildRegistrations();

        //
        // DM
        //

        // DM GLOBAL //

        @NotNull IRegisteredStringCommand setDMGlobal(boolean v);

        boolean isDMGlobal();

        // PER USER //

        @NotNull IRegisteredStringCommand setUserRegistrations(@Nullable Collection<User> users);

        @NotNull IRegisteredStringCommand addUserRegistration(@NotNull User user);

        @NotNull IRegisteredStringCommand removeUserRegistration(@NotNull User user);

        @NotNull List<User> getUserRegistrations();

    }

}
