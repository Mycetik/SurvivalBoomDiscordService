package net.survivalboom.sbds.moderation.api.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IMuteData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IMuteManager {

    //
    // ACTIONS
    //

    @NotNull CompletableFuture<@NotNull IMuteData> mute(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );

    @NotNull CompletableFuture<@NotNull IMuteData> mute(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );


    @NotNull CompletableFuture<@NotNull IAuditEntry> removeMute(

            @NotNull IMuteData mute,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    );


    @NotNull CompletableFuture<@NotNull List<IMuteData>> getCurrent(
            @Nullable Guild guild,
            @Nullable User user
    );

}
