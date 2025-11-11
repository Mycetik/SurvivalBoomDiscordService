package net.survivalboom.sbds.moderation.api.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IWarnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IWarnManager {

    @NotNull CompletableFuture<@NotNull IWarnData> warn(

            @NotNull Guild guild,
            @NotNull User user,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );

    @NotNull CompletableFuture<@NotNull IWarnData> warn(

            @NotNull Member member,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable Duration duration

    );


    @NotNull CompletableFuture<@NotNull IAuditEntry> removeWarn(

            @NotNull IWarnData warn,
            @Nullable User moderator,

            @Nullable String reason,
            @Nullable String comment

    );


    @NotNull CompletableFuture<@NotNull List<IWarnData>> getCurrent(
            @Nullable Guild guild,
            @Nullable User user
    );

    @NotNull CompletableFuture<@Nullable IWarnData> getById(@Nullable Guild guild, long id);

}
