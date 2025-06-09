package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.ISBDS;
import org.jetbrains.annotations.NotNull;

public interface GuildExecution {

    @NotNull ISBDS sbds();

    Guild guild();

    Member member();


    default boolean hasPermission(@NotNull String permission) {
        return sbds().getPermissionManager().hasPermission(member(), permission, false);
    }

}
