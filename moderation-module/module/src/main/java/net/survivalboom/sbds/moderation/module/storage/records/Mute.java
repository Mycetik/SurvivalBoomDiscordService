package net.survivalboom.sbds.moderation.module.storage.records;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.moderation.module.storage.Punishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.Temporal;

@Entity
@Table(name = "sbds_moderation_mutes")
public class Mute extends Punishment {

    public Mute(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User responsible,

            @NotNull Instant time,
            @Nullable Instant end

    ) {
        super(guild, user, reason, comment, responsible, time, end, false);
    }

    public Mute() {}

    public static @NotNull Mute create(
            @NotNull Guild guild,
            @NotNull User user,
            @NotNull Instant time,
            @Nullable Instant endTime,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {
        return new Mute(guild, user, reason, comment, responsible, time, endTime);
    }
}
