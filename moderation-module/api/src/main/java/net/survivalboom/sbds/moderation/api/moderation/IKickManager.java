package net.survivalboom.sbds.moderation.api.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IKickManager {

    //
    // ACTIONS
    //

    @NotNull CompletableFuture<@NotNull IAuditEntry> kick(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    );

    @NotNull CompletableFuture<@NotNull IAuditEntry> kick(

            @NotNull Guild guild,
            @NotNull User user,
            @NotNull User moderator,

            @Nullable String reason,
            @Nullable String comment

    );

}
