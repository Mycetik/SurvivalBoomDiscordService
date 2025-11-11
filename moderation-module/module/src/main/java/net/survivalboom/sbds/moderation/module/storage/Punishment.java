package net.survivalboom.sbds.moderation.module.storage;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.converters.GuildConverter;
import net.survivalboom.sbds.api.database.converters.UserConverter;
import net.survivalboom.sbds.moderation.api.moderation.PunishmentType;
import net.survivalboom.sbds.moderation.api.storage.IPunishmentData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class Punishment extends DataRecord implements IPunishmentData {

    @Id
    protected long id;


    @Column(nullable = false)
    @Convert(converter = GuildConverter.class)
    protected Guild guild;

    @Column(nullable = false)
    @Convert(converter = UserConverter.class)
    protected User user;


    @Column
    protected String reason;

    @Column
    protected String comment;


    @Column
    @Convert(converter = UserConverter.class)
    protected User moderator;


    @Column(nullable = false)
    protected Instant time;

    @Column
    protected Instant end;


    protected final PunishmentType typed;


    public Punishment(

            @NotNull Guild guild,
            @NotNull User user,

            @Nullable String reason,
            @Nullable String comment,

            @Nullable User moderator,

            @NotNull Instant time,
            @Nullable Instant end,

            boolean allowDuplication,
            PunishmentType type

    ) {

        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(user, "user == null");
        Objects.requireNonNull(time, "time == null");

        this.guild = guild;
        this.user = user;

        this.reason = reason;
        this.comment = comment;

        this.moderator = moderator;

        this.time = time;
        this.end = end;

        if (!allowDuplication) {
            this.id = hash(guild.getIdLong(), user.getIdLong());
        }

        else {
            this.id = hash(guild.getIdLong(), user.getIdLong(), time.getEpochSecond());
        }

        this.typed = type;

    }

    protected Punishment(
            PunishmentType type
    ) {
        this.typed = type;
    }


    //
    // ID
    //

    @Override
    public long getId() {
        return id;
    }

    @Override
    public @NotNull PunishmentType getType() {
        return typed;
    }

    //
    // GUILD/USER/MODERATOR
    //

    @Override
    public @NotNull Guild getGuild() {
        return guild;
    }

    @Override
    public @NotNull User getUser() {
        return user;
    }

    @Override
    public CacheRestAction<@Nullable Member> getMember() {
        return guild.retrieveMember(user);
    }

    @Override
    public @Nullable User getModerator() {
        return moderator;
    }

    //
    // REASON
    //

    @Override
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public @Nullable String getComment() {
        return comment;
    }


    //
    // TIME
    //


    @Override
    public @NotNull Instant getTime() {
        return time;
    }

    @Override
    public @Nullable Instant getEnd() {
        return end;
    }

    @Override
    public @Nullable Duration getDuration() {

        if (end == null) {
            return null;
        }

        return Duration.ofSeconds(end.minusSeconds(time.getEpochSecond()).getEpochSecond());

    }


    @Override
    public String toString() {
        return String.format("%s{id=%s, guild=%s, user=%s, responsible=%s, reason=%s, comment=%s, time=%s, end=%s}", getClass().getSimpleName(), id, guild, user, moderator, reason, comment, time, end);
    }

}
