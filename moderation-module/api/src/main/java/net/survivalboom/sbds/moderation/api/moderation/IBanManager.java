package net.survivalboom.sbds.moderation.api.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IBanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IBanManager {

    @NotNull CompletableFuture<@NotNull IBanData> ban(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );

    @NotNull CompletableFuture<@NotNull IBanData> ban(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );

    @NotNull CompletableFuture<@NotNull IAuditEntry> removeBan(

            @NotNull IBanData ban,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    );


    @NotNull CompletableFuture<@NotNull List<IBanData>> getCurrent(
            @Nullable Guild guild,
            @Nullable User user
    );

}
