package net.survivalboom.sbds.api.interaction;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessages;
import org.jetbrains.annotations.NotNull;

public interface InteractionHolder {

    @NotNull ISBDS sbds();

    @NotNull IMessages messages();


    Guild guild();

    @NotNull User user();

    Member member();

    Channel channel();


    default boolean hasPermission(@NotNull String permission) {
        return sbds().getPermissionManager().hasPermission(member(), permission, false);
    }

}
