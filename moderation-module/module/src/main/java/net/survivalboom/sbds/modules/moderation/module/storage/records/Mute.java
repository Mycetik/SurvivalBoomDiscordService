package net.survivalboom.sbds.modules.moderation.module.storage.records;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.modules.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.modules.moderation.api.storage.IMuteData;
import net.survivalboom.sbds.modules.moderation.module.storage.Punishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(name = "sbds_moderation_mutes")
public class Mute extends Punishment implements IMuteData {

    public Mute(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User responsible,

            @NotNull Instant time,
            @Nullable Instant end

    ) {
        super(guild, user, reason, comment, responsible, time, end, false, PunishmentType.MUTE);
    }

    protected Mute() {
        super(PunishmentType.MUTE);
    }

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
